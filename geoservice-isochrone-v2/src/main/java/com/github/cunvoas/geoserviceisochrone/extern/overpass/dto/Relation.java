package com.github.cunvoas.geoserviceisochrone.extern.overpass.dto;

import java.util.List;
import java.util.Map;

/**
 * Représente une relation OSM dans la réponse Overpass JSON.
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "type": "relation",
 *     "id": 789,
 *     "members": [
 *       { "type": "way", "ref": 456, "role": "outer" }
 *     ],
 *     "tags": { "type": "multipolygon" }
 *   }
 * </pre>
 */
public class Relation extends Element {
    public List<Member> members;
    public Map<String, String> tags;

    public static class Member {
        public String type;
        public Long ref;
        public String role;
        
        /**
         * Si le membre est inclus en tant qu'objet complet (Way), il sera désérialisé ici.
         * Peut être null si le membre n'est référencé que par id.
         */
        public Element element;
    }
}