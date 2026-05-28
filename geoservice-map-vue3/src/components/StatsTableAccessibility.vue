<template>
  <div v-if="loaded" class="sr-only" aria-live="polite">
    <table :aria-label="'Statistiques détaillées pour ' + villeNom">
      <caption>Répartition de la population par accès aux espaces verts à {{ villeNom }}</caption>
      <thead>
        <tr>
          <th scope="col">Seuil de surface (m²/hab)</th>
          <th scope="col">Nombre d'habitants</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in tableData" :key="index">
          <td>{{ item.label }}</td>
          <td>{{ item.value }} habitants</td>
        </tr>
      </tbody>
    </table>
    <p v-if="summaryText">{{ summaryText }}</p>
  </div>
</template>

<script>
export default {
  name: 'StatsTableAccessibility',
  props: {
    villeNom: {
      type: String,
      default: ''
    },
    dataPie: {
      type: Object,
      default: () => ({})
    },
    loaded: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    tableData() {
      if (!this.dataPie || !this.dataPie.labels || !this.dataPie.datasets) return [];
      const labels = this.dataPie.labels;
      const data = this.dataPie.datasets[0].data;
      return labels.map((label, index) => ({
        label,
        value: data[index]
      }));
    },
    summaryText() {
      if (!this.tableData.length) return '';
      // Exemple de synthèse : on cherche le seuil le plus élevé ou une moyenne
      // Ici on peut extraire des chiffres clés demandés par l'utilisateur
      const totalHab = this.tableData.reduce((acc, curr) => acc + curr.value, 0);
      if (totalHab === 0) return '';
      
      return `À ${this.villeNom}, la population est répartie sur différents seuils d'accessibilité aux espaces verts, permettant une analyse précise de la densité de m² par habitant.`;
    }
  }
}
</script>
