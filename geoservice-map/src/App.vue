<template>
  <div id="app" valign="top" align="left">
      <b-container>
        <b-row>
          <b-col>
            <img
              alt="Deul'Air"
              src="./assets/logo-deulair.png"
              height="100px"
              style="border: 2px"
            />
            </b-col>
          <b-col >
              <img
                alt="LM Oxygène"
                src="./assets/logo-lmo.png"
                height="100px"
                style="border: 2px" />
            </b-col>
        </b-row>

      </b-container>

      <span style="border: 2px" >
          <div id="map" align="center" valign="middle" style="position: relative, z-index: 10;">
            <Isochrone msg="Cartographie des parcs" />
          </div>
      </span>

    <span id="legend">
      <table class="legend" border="1px">
        <tr>
            <th style="color:#000000;">Légende Isochone </th>
            <th style="color:#000000;">Pourcentage de population avec un parc</th>
        </tr>
      <tbody>
        <tr>
          <td style="background-color:#2B100D;">OMS: n'est pas un parc</td>
          <td>n/a</td>
        </tr>
        <tr>
          <td style="background-color:#58D83E;">&gt; 12 m²/habitant (100%)</td>
          <td style="background-color:#58D83E;">&gt;= 98% à &lt;=100%</td>
        </tr>
        <tr>
          <td style="background-color:#D8C13E;">6 &lt; m²/habitant &lt;= 12 (&lt;>50%)</td>
          <td style="background-color:#D8C13E;">&gt;= 80% et &lt;98%</td>
        </tr>
        <tr>
          <td style="background-color:#D8783E;">3 &lt; m²/habitant &lt;= 6 (&lt;25%)</td>
          <td style="background-color:#D8783E;">&gt;= 20% et &lt;80%</td>
        </tr>
        <tr>
          <td style="background-color:#D84E3E;">&lt;= 3 m²/habitant (&gt;25%)</td>
          <td style="background-color:#D84E3E;">&lt;20%</td>
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
  color:#FFFFFF;
  padding:2px
}
table.legend td {
  color:#FFFFFF;
  padding:2px
}
</style>
