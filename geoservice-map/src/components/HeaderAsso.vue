
<template>
    <b-container>
           <b-row style="align: center;">
                <b-col style="vertical-align: top;">
                        <router-link to="/carte">Carte</router-link>
                        <br />
                        <router-link to="/surface">Surface</router-link>
                </b-col>
            
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/aut-mel"
                    target="_blank" >
                    <img
                        alt="Aut'MEL'"
                        src="../assets/logo-autmel.png"
                        height="100px"
                        style="border: 2px"
                /></a>
            </b-col>
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/deul-air"
                    target="_blank" >
                    <img
                        alt="Deul'Air"
                        src="../assets/logo-deulair.png"
                        height="100px"
                        style="border: 2px"
                /></a>
            </b-col>
            <b-col>
                <a
                    href="https://www.helloasso.com/associations/lm-oxygene"
                    target="_blank" >
                    <img
                        alt="LM Oxygène"
                        src="../assets/logo-lmo.png"
                        height="100px"
                        style="border: 2px"
                /></a>
            </b-col>
        </b-row>
        <b-row>
            <b-col>
                        <router-view></router-view>
            </b-col>
        </b-row>
    </b-container>
</template>