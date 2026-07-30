package com.github.cunvoas.geoserviceisochrone.service.proposal;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
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
                org.locationtech.jts.geom.Coordinate coord = new org.locationtech.jts.geom.Coordinate(
                    Double.parseDouble(form.getMapLng()),
                    Double.parseDouble(form.getMapLat())
                );
                org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory();
                proposal.setCentre(factory.createPoint(coord));
            } else if (StringUtils.isNotBlank(form.getSGeometry())) {
                org.locationtech.jts.geom.GeometryFactory factory = new org.locationtech.jts.geom.GeometryFactory();
                tools.jackson.databind.JsonNode root = new tools.jackson.databind.ObjectMapper().readTree(form.getSGeometry());
                if (root.has("coordinates")) {
                    double lng = root.get("coordinates").get(0).asDouble();
                    double lat = root.get("coordinates").get(1).asDouble();
                    proposal.setCentre(factory.createPoint(new org.locationtech.jts.geom.Coordinate(lng, lat)));
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
        meta.setTotalSurfaceOfParks(all.stream()
            .filter(p -> p.getSurface() != null)
            .mapToInt(p -> p.getSurface().intValue())
            .sum());
        metaRepository.save(meta);

        return proposal;
    }

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
                    meta.setTotalSurfaceOfParks(remaining.stream()
                        .filter(p -> p.getSurface() != null)
                        .mapToInt(p -> p.getSurface().intValue())
                        .sum());
                    metaRepository.save(meta);
                }
            });
        });
    }

    public List<ManualParkProposal> findByInsee(String insee) {
        Integer annee = java.time.Year.now().getValue();
        return findByInsee(insee, annee);
    }

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
