<template>
  <div class="product-gallery">
    <ProductImage
      :src="activeImage"
      :alt="alt"
      :height="height"
      :widths="[480, 720, 960]"
      :quality="85"
      sizes="(max-width: 768px) 100vw, 520px"
      @click="$emit('preview', activeIndex)"
    />
    <div v-if="normalizedImages.length > 1" class="product-gallery__thumbs">
      <button
        v-for="(image, index) in normalizedImages"
        :key="`${image}-${index}`"
        type="button"
        class="product-gallery__thumb"
        :class="{ 'is-active': index === activeIndex }"
        @click="activeIndex = index"
      >
        <img :src="getThumbUrl(image)" :alt="`${alt}-${index + 1}`" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import ProductImage from '@/components/ProductImage.vue'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  alt: {
    type: String,
    default: '商品图片'
  },
  height: {
    type: Number,
    default: 420
  }
})

defineEmits(['preview'])

const activeIndex = ref(0)
const normalizedImages = computed(() => props.images.filter(Boolean))
const activeImage = computed(() => normalizedImages.value[activeIndex.value] || '')
const getThumbUrl = image => imageUtils.getImageUrl(image, { width: 96, height: 96, quality: 75 })

watch(normalizedImages, () => {
  activeIndex.value = 0
})
</script>

<style scoped>
.product-gallery {
  display: grid;
  gap: var(--spacing-md);
}

.product-gallery__thumbs {
  display: flex;
  gap: var(--spacing-sm);
  overflow-x: auto;
}

.product-gallery__thumb {
  width: 64px;
  height: 64px;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  background: var(--color-bg-light);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
}

.product-gallery__thumb.is-active {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--color-primary-lighter);
}

.product-gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
