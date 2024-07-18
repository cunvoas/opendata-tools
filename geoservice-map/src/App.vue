<template>
    <div id="app" valign="top" align="left">
        <b-container>
            <b-row>
                <b-col>
                    <a
                        href="https://www.helloasso.com/associations/aut-mel"
                        target="_blank" >
                        <img
                            alt="Aut'MEL'"
                            src="./assets/logo-autmel.png"
                            height="100px"
                            style="border: 2px"
                    /></a>
                </b-col>
                <b-col>
                    <a
                        href="https://www.helloasso.com/associations/deul-air"
                        target="_blank" >
                        <img
                            alt="Deul'Air"
                            src="./assets/logo-deulair.png"
                            height="100px"
                            style="border: 2px"
                    /></a>
                </b-col>
                <b-col>
                    <a
                        href="https://www.helloasso.com/associations/lm-oxygene"
                        target="_blank" >
                        <img
                            alt="LM Oxygène"
                            src="./assets/logo-lmo.png"
                            height="100px"
                            style="border: 2px"
                    /></a>
                </b-col>
            </b-row>
        </b-container>

        <span style="border: 2px">
            <div
                id="map"
                align="center"
                valign="middle"
                style="position: relative, z-index: 10;"
            >
                <Isochrone msg="Cartographie des parcs" />
            </div>
        </span>

        <span id="legend">
            <table class="legend" border="1px">
                <tr>
                    <th style="color: #000000">
                        Légende Isochone (Valide OMS)
                    </th>
                </tr>
                <tbody>
                    <tr>
                        <td style="background-color: #1a9900">
                            &gt; 12 m²/habitant en ville (45 m²/hab à la
                            campagne)
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #9ee88f">
                            10 &lt; m²/habitant &lt;= 12 (entre 25 et 45 m²/hab
                            à la campagne)
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #d8973e">
                            &gt; 10 m²/habitant en ville (25 m²/hab à la
                            campagne) mais dont une partie de la population est
                            exclue.
                        </td>
                    </tr>
                    <tr>
                        <td
                            style="background: linear-gradient(to right, #7c7c7c, #e0e0e0);" >
                            &lt; 10 m²/habitant (&lt; 25 m²/hab à la campagne)
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #f1e2e2; color: black">
                            N'est pas un parc
                        </td>
                    </tr>
                    <tr>
                        <td style="background-color: #4944f5">
                            N'a pas été calculé
                        </td>
                    </tr>
                </tbody>
            </table>
        </span>
    </div>
</template>

<script>
import Isochrone from "./components/Isochrone.vue";

export default {
    name: "App",
    components: {
        Isochrone,
    },
};
</script>

<style>
#app {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    margin-top: 10px;
}
table.legend td {
    color: #ffffff;
    font-size: 12px;
    padding: 2px;
}

.dataDetail {
    padding: 6px 8px;
    font:
        16px/18px Arial,
        Helvetica,
        sans-serif;
    background: white;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
    border-radius: 5px;
}
.dataDetail h4 {
    margin: 0 0 5px;
    color: #777;
}
.legend {
    text-align: left;
    line-height: 18px;
    color: #555;
}
.legend i {
    width: 18px;
    height: 18px;
    float: left;
    margin-right: 8px;
    opacity: 0.7;
}
</style>
