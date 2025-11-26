<template>
    <div class="container mx-auto px-4 py-6">
        <!-- Header Title -->
        <div class="text-center mb-6">
            <h3 class="text-2xl font-bold text-gray-800">Parcs accessibles en m² par habitant</h3>
        </div>
        
        <!-- Logos Row -->
        <div class="flex justify-center items-center gap-10 mb-4">
            <a
                href="https://www.helloasso.com/associations/aut-mel"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Aut'MEL'"
                    src="../assets/logo-autmel.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/deul-air"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Deûl'Air"
                    src="../assets/logo-deulair.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/entrelianes"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="Entrelianes"
                    src="../assets/logo-entrelianes.png"
                    class="w-24 border-2 border-transparent"
                />
            </a>
            <a
                href="https://www.helloasso.com/associations/lm-oxygene"
                target="_blank" 
                rel="noopener noreferrer"
                class="hover:opacity-80 transition-opacity">
                <img
                    alt="LM Oxygène"
                    src="../assets/logo-lmo.png"
                    class="h-20 border-2 border-transparent"
                />
            </a>
        </div>
        
        <!-- Navigation Links -->
        <nav class="flex flex-wrap gap-2 items-center justify-center mb-6">
            <router-link 
                to="/carte" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Carte
            </router-link>
            <router-link 
                to="/stats" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Statistiques
            </router-link>
            <router-link 
                to="/Information" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Information
            </router-link>
            <router-link 
                to="/aideoVideo" 
                class="px-4 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition-colors font-medium no-underline"
                active-class="bg-yellow-50 text-black font-bold">
                Aide en vidéo
            </router-link>
        </nav>
        
        <!-- Router View -->
        <div class="w-full">
            <router-view></router-view>
        </div>
    </div>
</template>

<style scoped>
.no-underline {
  text-decoration: none;
}
</style>