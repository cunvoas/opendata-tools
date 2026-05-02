package com.github.cunvoas.geoserviceisochrone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import tools.jackson.databind.DeserializationFeature;

/**
 * Point d'entrée principal de l'application GeoserviceIsochrone (Spring Boot).
 * <p>
 * Configure le contexte Spring, le scan des composants et la personnalisation du mapping JSON.
 * </p>
 * <ul>
 *   <li>Active l'AOP (AspectJ)</li>
 *   <li>Configure le mapping JSON pour la tolérance sur les tableaux et valeurs nulles</li>
 *   <li>Permet l'extension par d'autres modules (metrics, etc.)</li>
 * </ul>
 *
 * @author cunvoas
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "com.github.cunvoas.geoserviceisochrone",
        "com.github.cunvoas.metrics"
})
@EnableAspectJAutoProxy
public class GeoserviceIsochroneApplication {

    /**
     * Méthode main : démarre l'application Spring Boot.
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(GeoserviceIsochroneApplication.class, args);
    }

    /**
     * Personnalise la configuration du mapping JSON (Jackson).
     * <ul>
     *   <li>Accepte les tableaux à un seul élément comme valeur unique</li>
     *   <li>Ignore les nulls pour les types primitifs</li>
     *   <li>Ignore les tableaux ou chaînes vides comme null</li>
     *   <li>Désactive l'utilisation de null pour les références manquantes</li>
     * </ul>
     * @return customizer Jackson
     */
    @Bean
    public JsonMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .disable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .disable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(DeserializationFeature.USE_NULL_FOR_MISSING_REFERENCE_VALUES);
    }

    // CORS config désactivée, voir doc si besoin d'ouvrir l'API à d'autres domaines
    /*
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                List<String> ss = List.of("http://localhost:8081", "https://autmel-maps.duckdns.org");
                registry
                    .addMapping("/map")
                    // FIXME to Variablize
                    .allowedOrigins("http://localhost:8081", "https://autmel-maps.duckdns.org");
            }
        };
    }
    */
}