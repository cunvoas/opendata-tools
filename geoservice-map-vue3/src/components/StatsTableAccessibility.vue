<template>
  <div v-if="loaded" class="w-full max-w-4xl mt-6">
    <table :aria-label="'Statistiques détaillées pour ' + villeNom" class="w-full text-sm border-collapse">
      <caption class="text-left font-semibold mb-2">Répartition de la population par accès aux espaces verts à {{ villeNom }}</caption>
      <thead>
        <tr class="bg-slate-100 text-left">
          <th scope="col" class="p-2 border border-slate-200">Seuil de surface (m²/hab)</th>
          <th scope="col" class="p-2 border border-slate-200">Nombre d'habitants</th>
          <th scope="col" class="p-2 border border-slate-200">Part</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(item, index) in tableData" :key="index">
          <th scope="row" class="p-2 border border-slate-200 text-left">{{ item.label }}</th>
          <td class="p-2 border border-slate-200">{{ item.value }} habitants</td>
          <td class="p-2 border border-slate-200">{{ item.part }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="summaryText" class="sr-only">{{ summaryText }}</p>
  </div>
</template>

<script>
import { formaterPourcentage } from '../utils/tranches.js';

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
      if (!this.dataPie?.labels || !this.dataPie.datasets?.[0]?.data) return [];
      const labels = this.dataPie.labels;
      const data = this.dataPie.datasets[0].data;
      const total = data.reduce((somme, valeur) => somme + (valeur || 0), 0);
      return labels.map((label, index) => ({
        label,
        value: data[index],
        part: formaterPourcentage(data[index] || 0, total)
      }));
    },
    summaryText() {
      if (!this.tableData.length) return '';
      const totalHab = this.tableData.reduce((acc, curr) => acc + curr.value, 0);
      if (totalHab === 0) return '';

      return `À ${this.villeNom}, la population est répartie sur différents seuils d'accessibilité aux espaces verts, permettant une analyse précise de la densité de m² par habitant.`;
    }
  }
}
</script>
