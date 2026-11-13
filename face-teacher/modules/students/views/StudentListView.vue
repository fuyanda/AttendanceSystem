<template>
  <PageContainer :style="fillStyle">
    <NavPageHeader title="学生名单管理" show-back @back="goHome" />
    <SearchBar
      :model-value="keyword"
      placeholder="搜索学生姓名或学号..."
      @update:model-value="onSearchInput"
      @search="onSearch"
    />
    <StudentListPanel
      :campus-groups="campusGroups"
      :search-students="searchStudents"
      :total="total"
      :loading="loading"
      :campus-loading="campusLoading"
      :is-searching="isSearching"
      :is-campus-collapsed="isCampusCollapsed"
      @load-more="loadMore"
      @edit="goEdit"
      @toggle-campus="toggleCampus"
      @campus-load-more="loadCampusStudents"
    />
    <StudentAddActionBar @click="goAdd" />
    <AppTabBar current="students" />
  </PageContainer>
</template>
<script setup>
import SearchBar from '@/shared/components/ui/SearchBar.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import AppTabBar from '@/shared/components/layout/AppTabBar.vue'
import StudentListPanel from '@/modules/students/components/StudentListPanel.vue'
import StudentAddActionBar from '@/modules/students/components/StudentAddActionBar.vue'
import { pageFillStyle } from '@/shared/composables/useLayoutInsets.js'
import { useStudentList } from '@/modules/students/composables/useStudentList.js'

const fillStyle = pageFillStyle()

const {
  campusGroups,
  campusLoading,
  searchStudents,
  isSearching,
  keyword,
  total,
  loading,
  loadCampusStudents,
  onSearch,
  onSearchInput,
  loadMore,
  isCampusCollapsed,
  toggleCampus,
  goAdd,
  goEdit
} = useStudentList()

function goHome() {
  uni.reLaunch({ url: '/pages/home/home' })
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}
:deep(.page-section) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding-top: 0;
}
</style>
