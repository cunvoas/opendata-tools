<template>
    <div class="p-2 bg-gray-50 rounded-md mb-2" v-if="currentLocation && currentLocation.com2coId">
        <h3 class="m-0 mb-2 text-base text-gray-800 font-semibold">
            Accès rapide aux statistiques : {{ currentLocation.com2coName || 'Communauté de commune' }}
        </h3>
        <div class="flex gap-2 flex-wrap justify-start">
            <button 
                v-for="graphType in graphTypes" 
                :key="graphType.id"
                @click="selectGraphType(graphType)"
                class="px-3 py-2 bg-emerald-600 text-white rounded border-none cursor-pointer text-sm transition-all duration-200 whitespace-nowrap hover:bg-emerald-700 hover:shadow-md active:scale-95"
                :class="{ 'bg-emerald-800 shadow-inner': selectedType === graphType.id }"
            >
                {{ graphType.name }}
            </button>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Com2coQuickAccess',
    props: {
        currentLocation: {
            type: Object,
            default: null
        }
    },
    data() {
        return {
            selectedType: null,
            graphTypes: [
                {
                    id: 'urbans',
                    name: 'Dense',
                    suffix: 'urbans'
                },
                {
                    id: 'suburbs',
                    name: 'Périurbain',
                    suffix: 'suburbs'
                },
                {
                    id: 'all',
                    name: 'Densités confondues',
                    suffix: 'all'
                }
            ]
        };
    },
    methods: {
        selectGraphType(graphType) {
            this.selectedType = graphType.id;
            
            // Emit an event with the graph type data
            const location = {
                ...this.currentLocation,
                locType: 'com2co',  // Force locType to com2co
                graphType: graphType.suffix
            };

            // 📊 Envoyer l'événement à Matomo
            if (window._paq && Array.isArray(window._paq)) {
                const com2coName = location.com2coName || 'Unknown';
                window._paq.push(['trackEvent', 'City Stats', 'com2co', com2coName]);
            }
            
            console.log("Com2coQuickAccess emitting:", JSON.stringify(location));
            
            this.$emit('graph-type-selected', location);
        }
    }
};
</script>