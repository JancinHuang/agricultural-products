<template>
  <div class="flowing-bg">
    <div class="wave wave1"></div>
    <div class="wave wave2"></div>
    <div class="wave wave3"></div>
    <div class="floating-shapes">
      <div v-for="index in 5" :key="index" :class="['shape', `shape${index}`]"></div>
    </div>
  </div>
</template>

<style scoped>
.flowing-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
}

.wave {
  position: absolute;
  width: 200%;
  height: 200%;
  top: -50%;
  left: -50%;
  opacity: 0.3;
  border-radius: 45%;
  animation: wave-rotate linear infinite;
}

.wave1 {
  background: linear-gradient(135deg, rgba(46, 125, 50, 0.8) 0%, rgba(76, 175, 80, 0.4) 100%);
  animation-duration: 25s;
}

.wave2 {
  background: linear-gradient(135deg, rgba(27, 58, 38, 0.6) 0%, rgba(46, 125, 50, 0.3) 100%);
  animation-duration: 35s;
  animation-direction: reverse;
}

.wave3 {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.5) 0%, rgba(27, 58, 38, 0.2) 100%);
  animation-duration: 45s;
}

@keyframes wave-rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.floating-shapes {
  position: absolute;
  width: 100%;
  height: 100%;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(5px);
  animation: float ease-in-out infinite;
}

.shape1 {
  width: 120px;
  height: 120px;
  top: 10%;
  left: 10%;
  animation-duration: 8s;
}

.shape2 {
  width: 80px;
  height: 80px;
  top: 60%;
  left: 5%;
  animation-duration: 10s;
  animation-delay: -2s;
}

.shape3 {
  width: 150px;
  height: 150px;
  top: 20%;
  right: 15%;
  animation-duration: 12s;
  animation-delay: -4s;
}

.shape4 {
  width: 60px;
  height: 60px;
  bottom: 15%;
  right: 20%;
  animation-duration: 9s;
  animation-delay: -1s;
}

.shape5 {
  width: 100px;
  height: 100px;
  bottom: 25%;
  left: 20%;
  animation-duration: 11s;
  animation-delay: -3s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  25% {
    transform: translateY(-20px) rotate(5deg);
  }
  50% {
    transform: translateY(0) rotate(0deg);
  }
  75% {
    transform: translateY(20px) rotate(-5deg);
  }
}
</style>
