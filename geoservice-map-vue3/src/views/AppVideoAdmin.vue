<template>
    <div id="appVideoAdmin" valign="top" align="left">
      <h5>Saisie des parcs et des entrés</h5>
      <p>
        Les vidéos ci-dessous vous montrent comment saisir les contours des parcs et les entrées de parc.
        <br />
        L'application des parcs est à disposition des associations contributrices.</p>
        

    <div class="video-controls">
      <button v-for="(source, index) in sources" 
              :key="index" 
              @click="changeVideo(index)"
              class="video-btn"
              type="button">
        {{ source.videoTitle }}
      </button>
    </div>
    <div class="player-wrapper">
      <h5 id="videoTitle">{{ currentVideo.videoTitle }}</h5>

      <video
        id="videoPlayer"
        ref="videoPlayer"
        class="video-js vjs-big-play-centered vjs-16-9"
        controls
        preload="auto"
      >
        <source :src="videoSource" type="video/webm" />
        <p class="vjs-no-js">
          Pour voir la vidéo, merci d'activer JavaScript
        </p>
      </video>
      </div>
    </div>
  </template>
  
  <script>
  import videojs from 'video.js';
  import 'video.js/dist/video-js.css';
  

  const sources = [
    { 
        videoTitle: 'Saisir le contour du parc',
        videoSource: 'https://github.com/cunvoas/opendata-tool-spare-data/raw/refs/heads/main/video-aide/SaisirContourParc_1024p.webm',
        poster:      'https://github.com/cunvoas/opendata-tool-spare-data/raw/refs/heads/main/video-aide/SaisirContourParc.png'
     },
    {
        videoTitle: 'Saisir les entrées de parc',
        videoSource: 'https://github.com/cunvoas/opendata-tool-spare-data/raw/refs/heads/main/video-aide/SaisirLesEntrees_1024p.webm',
        poster:      'https://github.com/cunvoas/opendata-tool-spare-data/raw/refs/heads/main/video-aide/SaisirLesEntrees.png'
     }
];


export default {
  name: "AppVideoAdmin",
  data() {
    return {
      player: null,
      currentVideo: sources[0],
      videoSource: sources[0].videoSource,
      poster: sources[0].poster,
      sources // Add sources to data to make it reactive
    }
  },
  methods: {
    changeVideo(index) {
      this.currentVideo = sources[index];
      this.videoSource = this.currentVideo.videoSource;
      this.poster = this.currentVideo.poster;
      
      if (this.player) {
        this.player.src({ src: this.videoSource, type: 'video/webm' });
        this.player.poster(this.poster);
        this.player.load();
        this.player.play();
      }
    }
  },
  mounted() {
    this.$nextTick(() => {
      if (this.$refs.videoPlayer) {
        this.player = videojs(this.$refs.videoPlayer, {
          fluid: true,
          responsive: true,
          controls: true,
          playbackRates: [0.5, 1, 1.25, 1.5]
        });
      }
    });
  },
  beforeUnmount() {
    if (this.player) {
      this.player.dispose()
    }
  }
}
  
  </script>
  
<style scoped>
  .player-wrapper {
    max-width: 800px;
    margin: 20px auto;
  }
  
  .video-js {
    width: 100%;
    height: auto;
    aspect-ratio: 16/9;
  }
  
  /* Styles pour les contrôles video.js */
  .video-js .vjs-control-bar {
    display: flex !important;
    visibility: visible !important;
    opacity: 1 !important;
    background-color: rgba(43, 51, 63, 0.7);
  }
  
  .video-js .vjs-big-play-button {
    background-color: rgba(43, 51, 63, 0.7);
    border: none;
    border-radius: 50%;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
  
  /* Styles pour les boutons de sélection de vidéo */
  .video-controls {
    margin: 1rem 0;
    text-align: center;
  }
  
  .video-btn {
    margin: 0 0.5rem;
    padding: 0.5rem 1rem;
    background: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  .video-btn:hover {
    background: #45a049;
  }
  </style>