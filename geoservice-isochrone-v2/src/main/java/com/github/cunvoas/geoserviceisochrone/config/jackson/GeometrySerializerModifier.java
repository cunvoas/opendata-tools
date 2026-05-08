package com.github.cunvoas.geoserviceisochrone.config.jackson;

import org.locationtech.jts.geom.Geometry;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * Modificateur Jackson 3.x : force l'utilisation de JtsGeometrySerializer
 * pour TOUS les sous-types de Geometry (Polygon, MultiPolygon, GeometryCollection, etc.).
 *
 * Nécessaire car Jackson 3 ne propage pas automatiquement le sérialiseur
 * enregistré pour un type parent vers ses sous-types concrets.
 */
public class GeometrySerializerModifier extends ValueSerializerModifier {

    private static final long serialVersionUID = 1L;

    private static final JtsGeometrySerializer GEOMETRY_SERIALIZER = new JtsGeometrySerializer();

    @Override
    public ValueSerializer<?> modifySerializer(
            SerializationConfig config,
            BeanDescription.Supplier beanDesc,
            ValueSerializer<?> serializer) {
        if (Geometry.class.isAssignableFrom(beanDesc.get().getBeanClass())) {
            return GEOMETRY_SERIALIZER;
        }
        return serializer;
    }
}
