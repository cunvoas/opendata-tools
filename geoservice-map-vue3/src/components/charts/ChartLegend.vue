<!--
https://medium.com/@jogarcia/vuejs-chartjs-customize-the-legend-5dcd5c392b73
-->
<template>
    <div class="legend-container">
      <span
        v-for="(legendValue, index) in legendValues"
        :key="`legend-badge-${index}`"
      >
        <span
          :style="{ 'background-color': legendValue.color }"
          class="badge"
        ></span>
        <span>{{ legendValue.label }}</span>
      </span>
    </div>
  </template>
  
  <script>
  export default {
    props: {
      legendValues: {
        type: Array,
        default: () => [],
      },
    },
  };
  </script>
  
  <style scoped>
  .legend-container {
    padding: 1em;
    display: flex;
    flex-wrap: wrap;
    width: 100%;
    gap: 14px;
    justify-content: center;
  }
  .badge {
    border-radius: 4.5px;
    padding: 0;
    width: 9px;
    height: 9px;
    display: inline-block;
    margin-right: 3px;
  }
  </style>