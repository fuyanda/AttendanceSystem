<template>
  <PageSection>
    <view class="student-list-panel__toolbar">
      <view class="student-list-panel__title">学生名单</view>
      <view class="student-list-panel__count">共 {{ total }} 名 · {{ campusGroups.length }} 个校区</view>
    </view>

    <scroll-view
      class="student-list-panel__scroll"
      scroll-y
      :style="scrollFillStyle"
      @scrolltolower="$emit('load-more')"
    >
      <!-- 加载校区中 -->
      <view v-if="campusLoading" class="student-list-panel__empty">加载校区中...</view>

      <!-- 搜索模式：平铺结果 -->
      <template v-else-if="isSearching">
        <view v-if="!searchStudents.length && !loading" class="student-list-panel__empty">
          未找到匹配的学生
        </view>
        <StudentManageCard
          v-for="item in searchStudents"
          :key="item.id"
          :student="item"
          @edit="$emit('edit', $event)"
        />
        <view v-if="loading" class="student-list-panel__empty">加载中...</view>
      </template>

      <!-- 正常模式：校区分组 -->
      <template v-else>
        <view v-if="!campusGroups.length && !campusLoading" class="student-list-panel__empty">
          暂无校区数据
        </view>
        <StudentCampusGroup
          v-for="group in campusGroups"
          :key="group.key"
          :name="group.name"
          :count="group.students.length"
          :total="group.total"
          :collapsed="isCampusCollapsed(group.key)"
          :loading="group.loading"
          :finished="group.finished"
          @toggle="$emit('toggle-campus', group.key)"
          @load-more="$emit('campus-load-more', group.key)"
        >
          <StudentManageCard
            v-for="item in group.students"
            :key="item.id"
            :student="item"
            @edit="$emit('edit', $event)"
          />
        </StudentCampusGroup>
      </template>
    </scroll-view>
  </PageSection>
</template>
<script setup>
import { computed } from 'vue'
import StudentCampusGroup from '@/modules/students/components/StudentCampusGroup.vue'
import StudentManageCard from '@/modules/students/components/StudentManageCard.vue'
import { useScrollFillHeight } from '@/shared/composables/useScrollFillHeight.js'

defineProps({
  campusGroups: { type: Array, default: () => [] },
  searchStudents: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  loading: { type: Boolean, default: false },
  campusLoading: { type: Boolean, default: false },
  isSearching: { type: Boolean, default: false },
  isCampusCollapsed: { type: Function, default: () => false }
})
defineEmits(['load-more', 'edit', 'toggle-campus', 'campus-load-more'])

const { scrollHeightPx } = useScrollFillHeight({
  anchorSelector: '.student-list-panel__toolbar',
  bottomReserveRpx: 242
})
const scrollFillStyle = computed(() => ({ height: `${scrollHeightPx.value}px` }))
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.student-list-panel__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 $space-32 $space-12;
  flex-shrink: 0;
}
.student-list-panel__title {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-secondary;
}
.student-list-panel__count {
  font-size: 24rpx;
  color: $color-text-muted;
}
.student-list-panel__scroll {
  width: 100%;
  padding: 0 $space-32;
  box-sizing: border-box;
}
.student-list-panel__empty {
  text-align: center;
  color: $color-text-muted;
  padding: 64rpx 0;
  font-size: 26rpx;
}
</style>
