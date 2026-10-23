<template>
  <div class="relative flex-1" v-if="displaySearchAddress">
    <input
      type="text"
      v-model="search"
      @input="onInput"
      @keydown="onKeydown"
      :placeholder="placeholder"
      :aria-label="inputLabel"
      :disabled="disabled"
      role="combobox"
      aria-autocomplete="list"
      aria-haspopup="listbox"
      :aria-expanded="showDropdown && filteredItems.length > 0 ? 'true' : 'false'"
      aria-controls="addr-listbox"
      :aria-activedescendant="activeDescId"
      class="input-field text-sm disabled:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-60"
    />
    <ul v-if="filteredItems.length && showDropdown"
        id="addr-listbox"
        role="listbox"
        :aria-label="inputLabel"
        class="absolute bg-white border border-gray-300 rounded-md shadow-lg mt-1 w-full max-h-[200px] overflow-y-auto z-[10000] list-none m-0 p-0">
      <li
        v-for="(item, index) in filteredItems"
        :key="item.id"
        :id="'addr-option-' + index"
        role="option"
        tabindex="-1"
        :aria-selected="index === activeIndex"
        :class="index === activeIndex ? 'bg-gray-100' : ''"
        @click="selectItem(item)"
        @keydown.enter.prevent="selectItem(item)"
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
      showDropdown: false,
      activeIndex: -1
    };
  },
  computed: {
    filteredItems() {
      return this.items.filter(item =>
        item.label.toLowerCase().includes(this.search.toLowerCase())
      );
    },
    inputLabel() {
      return this.placeholder || 'Rechercher une adresse';
    },
    activeDescId() {
      if (this.activeIndex < 0 || this.activeIndex >= this.filteredItems.length) return undefined;
      return 'addr-option-' + this.activeIndex;
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
      this.activeIndex = -1;
      const result = await this.fetchItems(this.search);
      this.items = result || [];
      this.$emit('update:modelValue', null);
    },
    /**
     * Navigation clavier dans la liste de suggestions (pattern ARIA combobox).
     * @param {KeyboardEvent} event - événement clavier de l'input.
     */
    onKeydown(event) {
      if (!this.showDropdown || !this.filteredItems.length) return;
      const last = this.filteredItems.length - 1;
      if (event.key === 'ArrowDown') {
        event.preventDefault();
        this.activeIndex = this.activeIndex >= last ? 0 : this.activeIndex + 1;
      } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        this.activeIndex = this.activeIndex <= 0 ? last : this.activeIndex - 1;
      } else if (event.key === 'Enter') {
        if (this.activeIndex >= 0 && this.activeIndex <= last) {
          event.preventDefault();
          this.selectItem(this.filteredItems[this.activeIndex]);
        }
      } else if (event.key === 'Escape') {
        this.showDropdown = false;
        this.activeIndex = -1;
      }
    },
    /**
     * Handles the selection of an item from the autocomplete list.
     * Emits the selected address item to the parent component and updates the input value.
     * @param {Object} item - The item selected by the user.
     */
    selectItem(item) {
      this.search = item.label;
      this.showDropdown = false;
      this.activeIndex = -1;
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