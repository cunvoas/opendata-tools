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
    margin: 20px 0;
    padding: 15px;
    background-color: #f5f5f5;
    border-radius: 8px;
}

.com2co-quick-access h3 {
    margin: 0 0 15px 0;
    font-size: 1.1em;
    color: #2c3e50;
}

.buttons-container {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    justify-content: center;
}

.com2co-button {
    padding: 10px 20px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 1em;
    transition: all 0.3s ease;
}

.com2co-button:hover {
    background-color: #359268;
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.com2co-button:active {
    transform: scale(0.98);
}

.com2co-button.active {
    background-color: #2c7a5b;
    box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.2);
}
</style>
