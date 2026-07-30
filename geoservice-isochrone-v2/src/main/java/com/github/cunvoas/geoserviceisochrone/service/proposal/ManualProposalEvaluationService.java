package com.github.cunvoas.geoserviceisochrone.service.proposal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkProposalWorkView;
import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualEvalWork;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualEvalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.AbstractComputationtrategy;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ManualProposalEvaluationService {

    @Autowired
    private ApplicationBusinessProperties applicationBusinessProperties;

    @Autowired
    private ServiceOpenData serviceOpenData;

    @Autowired
    private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

    @Autowired
    private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;

    @Autowired
    private ManualProposalService manualProposalService;

    @Autowired
    private ManualParkProposalMetaRepository manualParkProposalMetaRepository;

    @Autowired
    private ManualEvalWorkRepository manualEvalWorkRepository;

    @Transactional
    public GeoJsonRoot evaluate(String insee, Integer annee) {
        log.info("Manual proposal evaluation for INSEE={}, annee={}", insee, annee);
        GeoJsonRoot root = new GeoJsonRoot();
        if (annee == null) {
            log.warn("No annee provided for INSEE={}", insee);
            return root;
        }

        Boolean dense = serviceOpenData.isDistanceDense(insee);
        Integer urbanDistance = Integer.valueOf(dense
                ? applicationBusinessProperties.getOmsUrbanDistance()
                : applicationBusinessProperties.getOmsSubUrbanDistance());
        Double recoSquareMeterPerCapita = dense
                ? applicationBusinessProperties.getRecoUrbSquareMeterPerCapita()
                : applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();

        List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(insee, true);
        if (carreShapes == null || carreShapes.isEmpty()) {
            log.warn("No carreaux found for INSEE={}", insee);
            return root;
        }

        ManualParkProposalMeta meta = manualParkProposalMetaRepository.findByAnneeAndInsee(annee, insee);
        if (meta == null) {
            log.info("Creating meta for INSEE={}, annee={}", insee, annee);
            meta = new ManualParkProposalMeta();
            meta.setAnnee(annee);
            meta.setInsee(insee);
            meta.setTypeAlgo("MANUAL");
            meta = manualParkProposalMetaRepository.save(meta);
        }

        manualEvalWorkRepository.deleteByIdMeta(meta.getId());

        List<ManualParkProposal> proposals = manualProposalService.findByInsee(insee, annee);
        if (proposals == null || proposals.isEmpty()) {
            log.warn("No manual proposals found for INSEE={}", insee);
            return root;
        }

        List<ManualEvalWork> works = new ArrayList<>();

        for (InseeCarre200mOnlyShape shape : carreShapes) {
            Optional<InseeCarre200mComputedV2> oCarre = inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, shape.getIdInspire());
            if (oCarre.isEmpty()) continue;

            InseeCarre200mComputedV2 carre = oCarre.get();

            BigDecimal accessingPopulation = carre.getPopulationInIsochroneOms();
            if (accessingPopulation == null) accessingPopulation = BigDecimal.ZERO;
            BigDecimal localPopulation = carre.getPopAll();
            if (localPopulation == null) localPopulation = BigDecimal.ZERO;
            BigDecimal accessingSurface = carre.getSurfaceTotalParkOms();
            if (accessingSurface == null) accessingSurface = BigDecimal.ZERO;
            BigDecimal surfacePerCapita = carre.getSurfaceParkPerCapitaOms();
            if (surfacePerCapita == null) surfacePerCapita = BigDecimal.ZERO;

            BigDecimal missingSurface;
            if (accessingPopulation.compareTo(BigDecimal.ZERO) > 0) {
                double densiteMissing = Math.max(recoSquareMeterPerCapita - surfacePerCapita.doubleValue(), 0);
                missingSurface = BigDecimal.valueOf(densiteMissing * localPopulation.doubleValue());
            } else {
                missingSurface = BigDecimal.ZERO;
            }

            double newAccessingSurfaceD = accessingSurface.doubleValue();
            Point carreCentre = shape.getGeoPoint2d();

            for (ManualParkProposal prop : proposals) {
                Point propCentre = prop.getCentre();
                if (propCentre == null) continue;
                double proposalSurface = prop.getSurface() != null ? prop.getSurface().doubleValue() : 0;
                if (proposalSurface <= 0) continue;

                double dist = 1000 * DistanceHelper.crowFlyDistance(
                        carreCentre.getY(), carreCentre.getX(),
                        propCentre.getY(), propCentre.getX());

                if (dist < urbanDistance + AbstractComputationtrategy.SQUARE_DISTANCE) {
                    newAccessingSurfaceD += proposalSurface;
                }
            }

            double popForDensity = accessingPopulation.doubleValue() > 0
                    ? accessingPopulation.doubleValue()
                    : localPopulation.doubleValue();

            double newSurfacePerCapitaD = popForDensity > 0
                    ? newAccessingSurfaceD / popForDensity
                    : 0;
            double newMissingSurfaceD = Math.max(
                    recoSquareMeterPerCapita * localPopulation.doubleValue() - newAccessingSurfaceD, 0);

            ManualEvalWork work = new ManualEvalWork();
            work.setIdMeta(meta.getId());
            work.setIdInspire(shape.getIdInspire());
            work.setIsDense(dense);
            work.setLocalPopulation(localPopulation);
            work.setAccessingPopulation(accessingPopulation);
            work.setAccessingSurface(accessingSurface);
            work.setSurfacePerCapita(surfacePerCapita);
            work.setMissingSurface(missingSurface);
            work.setNewAccessingSurface(BigDecimal.valueOf(newAccessingSurfaceD));
            work.setNewSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapitaD));
            work.setNewMissingSurface(BigDecimal.valueOf(newMissingSurfaceD));
            works.add(work);

            GeoJsonFeature feature = new GeoJsonFeature();
            root.getFeatures().add(feature);
            feature.setGeometry(shape.getGeoShape());

            ParkProposalWorkView pv = new ParkProposalWorkView();
            pv.setIdInspire(shape.getIdInspire());
            pv.setIsDense(String.valueOf(dense));
            pv.setLocalPopulation(String.valueOf(localPopulation.intValue()));
            pv.setAccessingPopulation(String.valueOf(accessingPopulation.intValue()));
            pv.setSurface(String.valueOf(accessingSurface.intValue()));
            pv.setSurfacePerCapita(String.format("%.1f", surfacePerCapita));
            pv.setMissingSurface(String.valueOf(missingSurface.intValue()));
            pv.setNewSurface(String.valueOf((int) newAccessingSurfaceD));
            pv.setNewSurfacePerCapita(String.format("%.1f", newSurfacePerCapitaD));
            pv.setNewMissingSurface(String.valueOf((int) newMissingSurfaceD));
            feature.setProperties(pv);
        }

        manualEvalWorkRepository.saveAll(works);

        log.info("Evaluation complete: {} carreaux saved", works.size());
        return root;
    }

    @Transactional(readOnly = true)
    public GeoJsonRoot loadEvaluation(String insee, Integer annee) {
        log.info("Load manual evaluation for INSEE={}, annee={}", insee, annee);
        GeoJsonRoot root = new GeoJsonRoot();
        if (annee == null) {
            log.warn("No annee provided for INSEE={}", insee);
            return root;
        }

        ManualParkProposalMeta meta = manualParkProposalMetaRepository.findByAnneeAndInsee(annee, insee);
        if (meta == null) {
            log.warn("No meta found for INSEE={}, annee={}", insee, annee);
            return root;
        }

        List<ManualEvalWork> works = manualEvalWorkRepository.findByIdMetaOrderByIdInspire(meta.getId());
        if (works == null || works.isEmpty()) {
            log.warn("No eval work found for metaId={}", meta.getId());
            return root;
        }

        for (ManualEvalWork work : works) {
            Optional<InseeCarre200mOnlyShape> oShape = inseeCarre200mOnlyShapeRepository.findById(work.getIdInspire());
            if (oShape.isEmpty()) continue;

            InseeCarre200mOnlyShape shape = oShape.get();
            GeoJsonFeature feature = new GeoJsonFeature();
            root.getFeatures().add(feature);
            feature.setGeometry(shape.getGeoShape());

            ParkProposalWorkView pv = new ParkProposalWorkView();
            pv.setIdInspire(work.getIdInspire());
            pv.setIsDense(String.valueOf(work.getIsDense()));
            pv.setLocalPopulation(String.valueOf(work.getLocalPopulation().intValue()));
            pv.setAccessingPopulation(String.valueOf(work.getAccessingPopulation().intValue()));
            pv.setSurface(String.valueOf(work.getAccessingSurface().intValue()));
            pv.setSurfacePerCapita(String.format("%.1f", work.getSurfacePerCapita()));
            pv.setMissingSurface(String.valueOf(work.getMissingSurface().intValue()));
            pv.setNewSurface(String.valueOf(work.getNewAccessingSurface().intValue()));
            pv.setNewSurfacePerCapita(String.format("%.1f", work.getNewSurfacePerCapita()));
            pv.setNewMissingSurface(String.valueOf(work.getNewMissingSurface().intValue()));
            feature.setProperties(pv);
        }

        log.info("Loaded {} eval work results", works.size());
        return root;
    }
}
