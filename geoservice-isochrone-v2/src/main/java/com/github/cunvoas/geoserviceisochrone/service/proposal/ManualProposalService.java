package com.github.cunvoas.geoserviceisochrone.service.proposal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormManualProposal;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour gérer les propositions manuelles de parcs.
 * <p>
 * Ce service permet de sauvegarder, supprimer et récupérer des propositions
 * manuelles de parcs en fonction du code INSEE et de l'année.
 * </p>
 */
@Service
@Slf4j
public class ManualProposalService {

    @Autowired
    private ManualParkProposalMetaRepository metaRepository;

    @Autowired
    private ManualParkProposalRepository proposalRepository;

    @Autowired
    private GeoJson2GeometryHelper geoJsonHelper;

    @Autowired
    private com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

    /**
     * Sauvegarde une proposition manuelle de parc.
     * <p>
     * Cette méthode crée ou met à jour une proposition.
     * Si aucune métadonnée n'existe pour cette commune et année,
     * une nouvelle métadonnée est créée automatiquement.
     * </p>
     * 
     * @param form Formulaire contenant les données de la proposition
     * @return La proposition sauvegardée
     */
    @Transactional
    public ManualParkProposal save(FormManualProposal form) {
        String insee = form.getCodeInsee();
        Integer annee = form.getAnnee() != null ? form.getAnnee() : java.time.Year.now().getValue();

        ManualParkProposalMeta meta = metaRepository.findByAnneeAndInsee(annee, insee);
        if (meta == null) {
            meta = new ManualParkProposalMeta();
            meta.setAnnee(annee);
            meta.setInsee(insee);
            meta.setTypeAlgo("MANUAL");
            meta = metaRepository.save(meta);
        }

        ManualParkProposal proposal = new ManualParkProposal();
        if (form.getId() != null) {
            proposal = proposalRepository.findById(form.getId()).orElse(new ManualParkProposal());
        }
        proposal.setIdMeta(meta.getId());
        proposal.setName(form.getName());
        proposal.setMode(form.getMode());
        proposal.setDescription(form.getDescription());
        proposal.setCreatedDate(new Date());

        if ("POLYGON".equals(form.getMode()) && StringUtils.isNotBlank(form.getSGeometry())) {
            Geometry geom = geoJsonHelper.parseGeoman(form.getSGeometry());
            if (geom != null) {
                proposal.setCentre(geom.getCentroid());
                proposal.setContour(geom);
                if (form.getSurface() != null && !form.getSurface().isEmpty()) {
                    proposal.setSurface(new BigDecimal(form.getSurface()));
                } else {
                    Long realSurface = inseeCarre200mOnlyShapeRepository.getSurface(geom);
                    proposal.setSurface(BigDecimal.valueOf(realSurface != null ? realSurface : 0L));
                }
            }
        } else {
            if (form.getMapLat() != null && form.getMapLng() != null) {
                Coordinate coord = new Coordinate(
                    Double.parseDouble(form.getMapLng()),
                    Double.parseDouble(form.getMapLat())
                );
                GeometryFactory factory = new GeometryFactory();
                proposal.setCentre(factory.createPoint(coord));
            } else if (StringUtils.isNotBlank(form.getSGeometry())) {
                GeometryFactory factory = new GeometryFactory();
                tools.jackson.databind.JsonNode root = new tools.jackson.databind.ObjectMapper().readTree(form.getSGeometry());
                if (root.has("coordinates")) {
                    double lng = root.get("coordinates").get(0).asDouble();
                    double lat = root.get("coordinates").get(1).asDouble();
                    proposal.setCentre(factory.createPoint(new Coordinate(lng, lat)));
                }
            }

            if (form.getSurface() != null && !form.getSurface().isEmpty()) {
                proposal.setSurface(new BigDecimal(form.getSurface()));
            } else if (form.getDiameter() != null && !form.getDiameter().isEmpty()) {
                double radius = Double.parseDouble(form.getDiameter()) / 2.0;
                double area = Math.PI * radius * radius;
                proposal.setSurface(BigDecimal.valueOf(area));
            }
        }

        proposal = proposalRepository.save(proposal);

        List<ManualParkProposal> all = proposalRepository.findByIdMetaOrderByCreatedDateDesc(meta.getId());
        meta.setNumberOfParks(all.size());
        
        // STREAM EXPLICATION :
        // 1. .stream() : Transforme la liste 'all' en un flux d'éléments pour pouvoir utiliser les opérations fonctionnelles.
        // 2. .filter(p -> p.getSurface() != null) : Filtre la liste pour ne garder que les propositions qui ont une surface renseignée (évite les NullPointerException).
        // 3. .mapToInt(p -> p.getSurface().intValue()) : Convertit chaque objet proposal (p) en sa valeur entière de surface (Stream d'entiers primitifs IntStream).
        // 4. .sum() : Effectue l'addition de toutes les surfaces entières obtenues pour calculer la surface totale cumulée.
        meta.setTotalSurfaceOfParks(all.stream()
            .filter(p -> p.getSurface() != null)
            .mapToInt(p -> p.getSurface().intValue())
            .sum());
        metaRepository.save(meta);

        return proposal;
    }

    /**
     * Supprime une proposition manuelle de parc.
     * <p>
     * Si la proposition était la dernière pour une métadonnée,
     * alors la métadonnée est supprimée également.
     * </p>
     * 
     * @param proposalId ID de la proposition à supprimer
     */
    @Transactional
    public void delete(Long proposalId) {
        proposalRepository.findById(proposalId).ifPresent(proposal -> {
            Long metaId = proposal.getIdMeta();
            proposalRepository.delete(proposal);

            metaRepository.findById(metaId).ifPresent(meta -> {
                List<ManualParkProposal> remaining = proposalRepository.findByIdMetaOrderByCreatedDateDesc(metaId);
                if (remaining.isEmpty()) {
                    metaRepository.delete(meta);
                } else {
                    meta.setNumberOfParks(remaining.size());
                    
                    // STREAM EXPLICATION :
                    // On recalcule la surface totale des parcs restants après suppression.
                    // .filter() élimine les objets sans surface, .mapToInt() extrait la surface sous forme d'entier, et .sum() additionne le tout.
                    meta.setTotalSurfaceOfParks(remaining.stream()
                        .filter(p -> p.getSurface() != null)
                        .mapToInt(p -> p.getSurface().intValue())
                        .sum());
                    metaRepository.save(meta);
                }
            });
        });
    }

    /**
     * Récupère toutes les propositions manuelles pour une commune.
     * <p>
     * La méthode utilise l'année courante par défaut.
     * </p>
     * 
     * @param insee Code INSEE de la commune
     * @return Liste des propositions
     */
    public List<ManualParkProposal> findByInsee(String insee) {
        Integer annee = java.time.Year.now().getValue();
        return findByInsee(insee, annee);
    }

    /**
     * Récupère toutes les propositions manuelles pour une commune et une année spécifiques.
     * 
     * @param insee Code INSEE de la commune
     * @param annee Année des propositions
     * @return Liste des propositions
     */
    public List<ManualParkProposal> findByInsee(String insee, Integer annee) {
        ManualParkProposalMeta meta = metaRepository.findByAnneeAndInsee(annee, insee);
        if (meta == null) return List.of();
        return proposalRepository.findByIdMetaOrderByCreatedDateDesc(meta.getId());
    }

    public ManualParkProposalMeta findMetaByInsee(String insee) {
        Integer annee = java.time.Year.now().getValue();
        return metaRepository.findByAnneeAndInsee(annee, insee);
    }
}
