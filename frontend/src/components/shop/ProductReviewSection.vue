<template>
  <section class="product-review-section">
    <div class="product-review-section__header">
      <h2>商品评价</h2>
      <div class="product-review-section__actions">
        <el-select :model-value="sort" size="small" style="width: 120px" @change="$emit('update:sort', $event)">
          <el-option label="最新" value="newest" />
          <el-option label="最热" value="hottest" />
        </el-select>
        <el-button v-if="canReview" type="primary" size="small" @click="$emit('show-review')">发表评价</el-button>
      </div>
    </div>

    <div v-if="reviews.length > 0" class="product-review-section__list">
      <article v-for="review in reviews" :key="review.id" class="product-review">
        <div class="product-review__header">
          <el-avatar :src="getImageUrl(review.userAvatar)" :size="40">{{ review.userName }}</el-avatar>
          <div class="product-review__info">
            <div class="product-review__user">{{ review.userName }}</div>
            <el-rate :model-value="review.rating" disabled />
          </div>
          <div class="product-review__time">{{ formatTime(review.createTime) }}</div>
          <div v-if="isOwner(review)" class="product-review__actions">
            <el-button type="primary" link size="small" @click="$emit('edit', review)">编辑</el-button>
            <el-button type="danger" link size="small" @click="$emit('delete', review)">删除</el-button>
          </div>
        </div>

        <div class="product-review__content">{{ review.content }}</div>
        <div v-if="getReviewImageList(review).length" class="product-review__images">
          <el-image
            v-for="(img, index) in getReviewImageList(review)"
            :key="index"
            :src="getImageUrl(img)"
            :preview-src-list="getReviewImageList(review).map(i => getImageUrl(i))"
            fit="cover"
            class="product-review__image"
          />
        </div>

        <div class="product-review__footer">
          <button type="button" class="product-review__link" @click="$emit('toggle-like', review)">
            <el-icon :class="{ liked: review.isLiked }"><Pointer /></el-icon>
            <span>{{ review.likeCount || 0 }}</span>
          </button>
          <button type="button" class="product-review__link" @click="$emit('reply', review)">
            <el-icon><ChatDotRound /></el-icon>
            <span>回复</span>
          </button>
        </div>

        <div v-if="review.replies && review.replies.length > 0" class="product-review__replies">
          <div v-for="reply in review.replies" :key="reply.id" class="product-review__reply">
            <div class="product-review__reply-header">
              <el-avatar :src="getImageUrl(reply.userAvatar)" :size="28">{{ reply.userName }}</el-avatar>
              <span>{{ reply.userName }}</span>
              <time>{{ formatTime(reply.createTime) }}</time>
              <el-button v-if="isOwner(reply)" type="danger" link size="small" @click="$emit('delete', reply)">删除</el-button>
            </div>
            <div class="product-review__reply-content">{{ reply.content }}</div>
            <button type="button" class="product-review__link product-review__link--small" @click="$emit('toggle-like', reply)">
              <el-icon :class="{ liked: reply.isLiked }"><Pointer /></el-icon>
              <span>{{ reply.likeCount || 0 }}</span>
            </button>
          </div>
        </div>
      </article>
    </div>
    <EmptyState v-else description="暂无评价" />
  </section>
</template>

<script setup>
import { Pointer, ChatDotRound } from '@element-plus/icons-vue'
import EmptyState from '@/components/base/EmptyState.vue'
import { formatTime } from '@/utils/time'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  reviews: {
    type: Array,
    default: () => []
  },
  sort: {
    type: String,
    default: 'newest'
  },
  canReview: {
    type: Boolean,
    default: false
  },
  currentUserId: {
    type: [Number, String],
    default: null
  }
})

defineEmits(['update:sort', 'show-review', 'edit', 'delete', 'toggle-like', 'reply'])

const getImageUrl = url => imageUtils.getImageUrl(url)
const splitImages = images => images ? images.split(',').map(item => item.trim()).filter(Boolean) : []
const getReviewImageList = review => splitImages(review.imageUrls || review.images)
const isOwner = review => props.currentUserId && review.userId === props.currentUserId
</script>

<style scoped>
.product-review-section {
  padding: 28px 32px;
  background: var(--color-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.product-review-section__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  padding-bottom: var(--spacing-md);
  border-bottom: 1px solid var(--color-border-light);
}

.product-review-section__header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 700;
}

.product-review-section__actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.product-review {
  padding: var(--spacing-md) 0;
  border-bottom: 1px solid var(--color-border-light);
}

.product-review:last-child {
  border-bottom: 0;
}

.product-review__header {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-sm);
}

.product-review__info {
  flex: 1;
  margin-left: var(--spacing-sm);
}

.product-review__user {
  margin-bottom: 2px;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.product-review__time {
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}

.product-review__actions {
  margin-left: var(--spacing-md);
}

.product-review__content {
  margin-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.7;
}

.product-review__images {
  display: flex;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.product-review__image {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
}

.product-review__footer {
  display: flex;
  gap: var(--spacing-xl);
  margin-top: var(--spacing-sm);
}

.product-review__link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: var(--font-size-sm);
  transition: color var(--transition-fast);
}

.product-review__link:hover {
  color: var(--color-primary);
}

.product-review__link .liked {
  color: var(--color-danger);
}

.product-review__link--small {
  margin-top: 6px;
  font-size: var(--font-size-xs);
}

.product-review__replies {
  margin-top: var(--spacing-md);
  margin-left: 50px;
  padding-left: var(--spacing-md);
  border-left: 2px solid var(--color-border-light);
}

.product-review__reply {
  padding: var(--spacing-sm) 0;
}

.product-review__reply-header {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: 6px;
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.product-review__reply-header time {
  color: var(--color-text-placeholder);
  font-size: 11px;
  font-weight: 400;
}

.product-review__reply-header .el-button {
  margin-left: auto;
}

.product-review__reply-content {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
</style>
