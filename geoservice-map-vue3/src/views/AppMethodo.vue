<template>
    <div class="text-left align-top">
        <div class="border-2 border-transparent">
            <div class="text-center relative z-10">
                <BarGraph msg="Barre surface par habitants" />
            </div>
        </div>
    </div>
</template>

<script>
import HeaderAsso from "../components/HeaderAsso.vue";
import BarGraph from "../components/BarGraph.vue";

export default {
    name: "AppMethodo",
    components: {
    HeaderAsso
    },
};
</script>
