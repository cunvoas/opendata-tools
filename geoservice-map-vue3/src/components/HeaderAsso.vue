
<template>
    <b-container name="bContainer">
          <b-row  style="align: center;text-align: center;vertical-align: middle;">
            <h3>Parcs accessibles en m² par habitant</h3>
          </b-row>
          
           <b-row  style="align: center;text-align: center;vertical-align: middle;">
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/aut-mel"
                    target="_blank" rel="noopener noreferrer">
                    <img
                        alt="Aut'MEL'"
                        src="../assets/logo-autmel.png"
                        height="80px"
                        style="border: 2px"
                /></a>
            </b-col>
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/deul-air"
                    target="_blank" rel="noopener noreferrer">
                    <img
                        alt="Deûl'Air"
                        src="../assets/logo-deulair.png"
                        height="80px"
                        style="border: 2px"
                /></a>
            </b-col>
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/entrelianes"
                    target="_blank" rel="noopener noreferrer">
                    <img
                        alt="Entrelianes"
                        src="../assets/logo-entrelianes.png"
                        width="96px"
                        style="border: 2px"
                        valign="middle"
                        
                /></a>
            </b-col>
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/lm-oxygene"
                    target="_blank" rel="noopener noreferrer">
                    <img
                        alt="LM Oxygène"
                        src="../assets/logo-lmo.png"
                        height="80px"
                        style="border: 2px"
                /></a>
            </b-col>
        
            <b-col name="bNav" style="vertical-align: middle;" id="router-links">
                <nav>  
                    <router-link class="router-links" to="/carte">Carte</router-link>
                    &nbsp;
                    <router-link class="router-links" to="/stats">Statistiques</router-link>
                    &nbsp;
                    <router-link class="router-links" to="/Information">Information</router-link>
                    &nbsp;
                    <router-link class="router-links" to="/aideoVideo">Aide en video</router-link>
                 </nav>
            </b-col>
        </b-row>
            
        <b-row name="bView">
            <b-col>
                   <router-view></router-view>
            </b-col>
        </b-row>
    </b-container>
</template>


<style scoped>
a {
  text-decoration: none; /* Remove default underlines */
  color: #222222;
  font-family: "Helvetica Neue", Arial, Helvetica, sans-serif;
}

a.router-link-exact-active {
  color: #000000;
  background-color: #fffff2; /* Example styling */
}

a.router-link-active {
  color: #000000; /* Example styling for nested routes */
  font-weight: bold;
}
</style>