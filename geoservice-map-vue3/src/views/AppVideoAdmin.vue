<template>
    <div class="text-left align-top">
      <h5 class="text-xl font-semibold mb-3">Saisie des parcs et des entrées</h5>
      <p class="mb-4">
        Les vidéos ci-dessous vous montrent comment saisir les contours des parcs et les entrées de parc.
        <br />
        L'application des parcs est à disposition des associations contributrices.
      </p>

      <div class="my-4 text-center">
        <button 
          v-for="(source, index) in sources" 
          :key="index" 
          @click="changeVideo(index)"
          class="mx-2 px-4 py-2 bg-green-600 text-white border-none rounded cursor-pointer hover:bg-green-700 transition-colors"
          type="button">
          {{ source.videoTitle }}
        </button>
      </div>

      <div class="max-w-[800px] mx-auto my-5">
        <h5 class="text-lg font-medium mb-2">{{ currentVideo.videoTitle }}</h5>

        <video
          id="videoPlayer"
          ref="videoPlayer"
          class="video-js vjs-big-play-centered vjs-16-9 w-full h-auto aspect-video"
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

      window._paq.push(['trackEvent', 'Video Changed', index, this.currentVideo.videoTitle]);
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
/* Keep video.js specific styles */
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
</style>