package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

class TestMessageFormat {
	
	String tpl="Bonjour @fullName@ et bienvenue sur le site de gestion des Parcs et Jardins d'Aut'mel.\n"
			+ "\n"
			+ "Votre compte <b>@login@</b> a été créé sur le site de gestion des parcs et jardins.<br />\n"
			+ "Dans le cas où votre mot de passe a été généré ou réinitialiser par le site, vous le recevrez dans un autre message.<br />\n"
			+ "<br />\n"
			+ "\n"
			+ "\n"
			+ "Le site Aut'mel de gestion des parcs et jardin est <b><a target=\"_blank\" href=\"@gestion@\">@gestion@</a></b>.<br />\n"
			+ "La restitution publique des données est sur le site <b><a target=\"_blank\" href=\"@public@\">@public@</a></b>.<br />\n"
			+ "\n"
			+ "<br />\n"
			+ "nb:<br />\n"
			+ "- Votre compte sera bloqué pendant 2h après 3 tentatives infructueuses.<br />\n"
			+ "- Si vous avez perdu votre mot de passe, il faudra en créer un nouveau, le site ne les stocke pas par raison de sécurité.<br />\n"
			+ "- Les fonctionnalités sont encore en cours de développement et elles seront déployées au fur et à mesure.<br />\n"
			+ "  * les détails sont sous le tableau de bord.<br />\n"
			+ "\n"
			+ "<br />\n"
			+ "Aut'mel,<br />\n"
			+ "<img src=\"cid:logo\" width=\"150\" height=\"150\" />\n"
			+ "";

	@Test
	void test01() {
		List<String> values =  List.of("v1", "v2", "v3");
		String formated = MessageFormat.format("a={0} b={1} c={2}", values.toArray());
		
		assertEquals("a=v1 b=v2 c=v3", formated);
	}
	
	@Test
	void test02() {
		List<String> values =  List.of("v1", "v2", "v3");
		String formated = MessageFormat.format("a={0} b={1} <a href=\"{2}\">{2}</a>", values.toArray());
		
		assertEquals("a=v1 b=v2 <a href=\"v3\">v3</a>", formated);
	}

	
	@Test
	void test03() {
		Map<String, String> map = new HashMap<>();
		map.put("@login@", "monLogin");
		map.put("@fullName@", "monNom");
		map.put("@gestion@", "https://site/gestion");
		map.put("@public@", "https://site/public");
		
		
		assertEquals("a=v1 b=v2 <a href=\"v3\">v3</a>", apply(tpl, map));
	}
	
	public String apply(String tpl, Map<String, String> vals) {
		//make a clone
		String ret =String.valueOf(tpl);
		for (Entry<String, String> entry : vals.entrySet()) {
			ret = ret.replaceAll(entry.getKey(), entry.getValue());
		}
		return ret;
		
	}

}
