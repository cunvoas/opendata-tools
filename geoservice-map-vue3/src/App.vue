<template>
    <div id="app">
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
nav a:link {color:#000000;}
nav a:visited {color:#000000;}
nav a:hover {color:#000000;}
nav a:active {color:#000000;}

#router-links {
    font-family: Avenir, Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
    a:link {color:#000000;};   
    a:visited {color:#000000;};
    a:hover {color:#000000;};   
    a:active {color:#000000;};  
}

</style>
