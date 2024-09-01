<template>
    <div id="appSurface" valign="top" align="left">
        
        <span style="border: 2px">
            <div
                id="surfaceBar"
                align="center"
                valign="middle"
                style="position: relative, z-index: 10;"
            >
                <BarGraph msg="Barre surface par habitants" />
            </div>
            
            <!--
            <div
                id="surfacePie"
                align="center"
                valign="middle"
                style="position: relative, z-index: 10;"
            >
                <PieGraph msg="camenbert surface par habitants" />
            </div>
            -->
        </span>
    
    </div>
</template>

<script>
import HeaderAsso from "../components/HeaderAsso.vue";
import BarGraph from "../components/BarGraph.vue";
//import PieGraph from "../components/PieGraph.vue";

export default {
    name: "AppSurface",
    components: {
    HeaderAsso,
    BarGraph,
//    PieGraph,
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

</style>
