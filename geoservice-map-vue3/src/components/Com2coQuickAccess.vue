<template>
    <div class="com2co-quick-access" v-if="currentLocation && currentLocation.com2coId">
        <h3>Accès rapide aux statistiques : {{ currentLocation.com2coName || 'Communauté de commune' }}</h3>
        <div class="buttons-container">
            <button 
                v-for="graphType in graphTypes" 
                :key="graphType.id"
                @click="selectGraphType(graphType)"
                class="com2co-button"
                :class="{ 'active': selectedType === graphType.id }"
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
                    name: 'Densité confondues',
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
            
            console.log("Com2coQuickAccess emitting:", JSON.stringify(location));
            
            this.$emit('graph-type-selected', location);
        }
    }
};
</script>

<style scoped>
.com2co-quick-access {
    padding: 8px;
    background-color: #f8f9fa;
    border-radius: 6px;
    margin-bottom: 8px;
}

.com2co-quick-access h3 {
    margin: 0 0 8px 0;
    font-size: 0.95em;
    color: #2c3e50;
    font-weight: 600;
}

.buttons-container {
    display: flex;
    gap: 6px;
    flex-wrap: wrap;
    justify-content: flex-start;
}

.com2co-button {
    padding: 6px 12px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 0.85em;
    transition: all 0.2s ease;
    white-space: nowrap;
}

.com2co-button:hover {
    background-color: #359268;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.com2co-button:active {
    transform: scale(0.98);
}

.com2co-button.active {
    background-color: #2c7a5b;
    box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.2);
}
</style>
