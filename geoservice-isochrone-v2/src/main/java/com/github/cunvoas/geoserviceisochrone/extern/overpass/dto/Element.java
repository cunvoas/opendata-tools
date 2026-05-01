package com.github.cunvoas.geoserviceisochrone.extern.overpass.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Élément abstrait Overpass (node, way, relation).
 * <p>
 * Utilisé pour la désérialisation polymorphe des objets du champ "elements" dans la réponse Overpass JSON.
 * Les sous-classes concrètes sont :
 * <ul>
 *   <li>{@link Node} – nœud OSM</li>
 *   <li>{@link Way} – chemin OSM</li>
 *   <li>{@link Relation} – relation OSM</li>
 * </ul>
 *
 * Exemple de structure JSON :
 * <pre>
 *   {
 *     "type": "node",
 *     "id": 123,
 *     ...
 *   }
 * </pre>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Node.class, name = "node"),
        @JsonSubTypes.Type(value = Way.class, name = "way"),
        @JsonSubTypes.Type(value = Relation.class, name = "relation")
})
public abstract class Element {
    public long id;
    public String type;

    
    public Bounds bounds;

}