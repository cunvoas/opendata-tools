<template>
  <div class="relative flex-1" v-if="displaySearchAddress">
    <input
      type="text"
      v-model="search"
      @input="onInput"
      :placeholder="placeholder"
      :disabled="disabled"
      class="input-field text-sm disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
    />
    <ul v-if="filteredItems.length && showDropdown" 
        class="absolute bg-white border border-gray-300 rounded-md shadow-lg mt-1 w-full max-h-[200px] overflow-y-auto z-[10000] list-none m-0 p-0">
      <li
        v-for="item in filteredItems"
        :key="item.id"
        @click="selectItem(item)"
        class="px-3 py-2 cursor-pointer flex justify-between items-center gap-3 hover:bg-gray-100 transition-colors"
      >
        <span class="flex-1">{{ item.label }}</span>
        <span v-if="item.score" class="text-xs text-gray-600 bg-gray-100 px-2 py-1 rounded font-medium whitespace-nowrap">
          {{ Math.round(item.score * 100) }}%
        </span>
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
    disabled: Boolean,
    displaySearchAddress: {
      type: Boolean,
      default: true
    },
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
    /**
     * Watcher for the `modelValue` prop.
     * Triggered whenever the value of `modelValue` changes.
     * @param {any} newValue - The updated value of the `modelValue` prop.
     */
    modelValue(newValue) {
      const selectedItem = this.items.find(item => item.id === newValue);
      this.search = selectedItem ? selectedItem.label : '';
    }
  },
  methods: {
    /**
     * Handles the input event for the search address autocomplete component.
     * Performs asynchronous operations such as fetching suggestions
     * based on the current input value.
     */
    async onInput() {
      this.showDropdown = true;
      const result = await this.fetchItems(this.search);
      this.items = result || [];
      this.$emit('update:modelValue', null);
    },
    /**
     * Handles the selection of an item from the autocomplete list.
     * Emits the selected address item to the parent component and updates the input value.
     * @param {Object} item - The item selected by the user.
     */
    selectItem(item) {
      this.search = item.label;
      this.showDropdown = false;
      this.$emit('update:modelValue', item.id);

      const coords = item.id.split(', ');
      const loc = {
          "locType": "address",
          "lonX": coords[0],
          "latY": coords[1]
      };
      //console.log("selectItem.emit"+JSON.stringify(loc));
      this.$emit('location-selected', loc);
    }
  }
};
</script>