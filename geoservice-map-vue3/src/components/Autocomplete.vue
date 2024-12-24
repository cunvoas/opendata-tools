<template>
  <div class="autocomplete">
    <input
      type="text"
      v-model="search"
      @input="onInput"
      :placeholder="placeholder"
      :disabled="disabled"
    />
    <ul v-if="filteredItems.length && showDropdown">
      <li
        v-for="item in filteredItems"
        :key="item.id"
        @click="selectItem(item)"
      >
        {{ item.label }}
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  props: {
    modelValue: [String, Number],
    fetchItems: Function,
    placeholder: String,
    disabled: Boolean
  },
  data() {
    return {
      search: '',
      items: [],
      showDropdown: false
    };
  },
  computed: {
    filteredItems() {
      return this.items.filter(item =>
        item.label.toLowerCase().includes(this.search.toLowerCase())
      );
    }
  },
  watch: {
    modelValue(newValue) {
      const selectedItem = this.items.find(item => item.id === newValue);
      this.search = selectedItem ? selectedItem.label : '';
    }
  },
  methods: {
    async onInput() {
      this.showDropdown = true;
      this.items = await this.fetchItems(this.search);
      this.$emit('update:modelValue', null);
    },
    selectItem(item) {
      this.search = item.label;
      this.showDropdown = false;
      this.$emit('update:modelValue', item.id);

      const coords = item.id.split(', ');
      const loc = {
          locType: 'address',
          lonX: coords[0],
          latY: coords[1]
      };
      //console.log("selectItem.emit"+JSON.stringify(loc));
      this.$emit('location-selected', loc);
    }
  }
};
</script>

<style scoped>
.autocomplete {
  position: relative;
}
.autocomplete ul {
  position: absolute;
  background: white;
  border: 1px solid #ccc;
  list-style: none;
  margin: 0;
  padding: 0;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  z-index: 10000;
}
.autocomplete li {
  padding: 8px;
  cursor: pointer;
}
.autocomplete li:hover {
  background: #f0f0f0;
}
</style>