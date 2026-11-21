import { describe, it, expect } from 'vitest';
import { mount } from '@vue/test-utils';
import StatsTableAccessibility from '../StatsTableAccessibility.vue';

const mountWith = (dataPie) =>
  mount(StatsTableAccessibility, { props: { villeNom: 'Lille', loaded: true, dataPie } });

describe('StatsTableAccessibility', () => {
  it('rend les lignes du camembert', () => {
    const wrapper = mountWith({
      labels: ['Faible', 'Forte'],
      datasets: [{ data: [30, 70] }]
    });

    const rows = wrapper.findAll('tbody tr');
    expect(rows).toHaveLength(2);
    expect(rows[0].text()).toContain('30 habitants');
    expect(rows[0].text()).toContain('30 %');
  });

  it('ne lève pas d’erreur quand datasets est vide', () => {
    const wrapper = mountWith({ labels: ['Faible'], datasets: [] });

    expect(wrapper.vm.tableData).toEqual([]);
    expect(wrapper.findAll('tbody tr')).toHaveLength(0);
  });

  it('ne lève pas d’erreur quand le camembert est absent', () => {
    const wrapper = mountWith({});

    expect(wrapper.vm.tableData).toEqual([]);
  });
});
