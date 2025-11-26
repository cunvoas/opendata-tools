<template>
    <div class="min-h-screen bg-white text-gray-800">
        <HeaderAsso></HeaderAsso>
    </div>
</template>
       

<script>
import HeaderAsso from "../components/HeaderAsso.vue";

export default {
    name: "app",
    components: {
    HeaderAsso
},
};
</script>

<style>
/* Keep only Leaflet-specific global styles */
.dataDetail {
    padding: 6px 8px;
    font: 16px/18px Arial, Helvetica, sans-serif;
    background: white;
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 0 15px rgba(0, 0, 0, 0.2);
    border-radius: 5px;
}

.dataDetail h4 {
    margin: 0 0 5px;
    color: #777;
}

table.legend td {
    color: #ffffff;
    font-size: 12px;
    padding: 2px;
}
</style>
