<template>
  <PageContainer>
    <NavPageHeader title="个人信息" show-back @back="goBack" />
    <PageSection card>
      <view v-if="loading" class="profile-hint">加载中...</view>
      <template v-else>
        <FormField label="登录账号">
          <view class="readonly">{{ form.userName || '-' }}</view>
        </FormField>
        <FormField label="归属校区">
          <view class="readonly">{{ form.deptName || '-' }}</view>
        </FormField>
        <FormField label="角色">
          <view class="readonly">{{ roleGroup || '-' }}</view>
        </FormField>
        <FormField label="昵称">
          <input v-model="form.nickName" class="app-input" placeholder="请输入昵称" />
        </FormField>
        <FormField label="手机号">
          <input v-model="form.phonenumber" class="app-input" type="number" placeholder="请输入手机号" />
        </FormField>
        <FormField label="邮箱">
          <input v-model="form.email" class="app-input" placeholder="请输入邮箱" />
        </FormField>
        <AppButton :loading="saving" @click="saveProfile">保存资料</AppButton>
        <AppButton type="ghost" @click="goPassword">修改密码</AppButton>
      </template>
    </PageSection>
  </PageContainer>
</template>
<script setup>
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PageSection from '@/shared/components/layout/PageSection.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import FormField from '@/shared/components/ui/FormField.vue'
import { useProfile } from '@/modules/settings/composables/useProfile.js'
const { loading, saving, form, roleGroup, saveProfile, goBack, goPassword } = useProfile()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.app-input, .readonly {
  height: $input-height;
  padding: 0 $space-24;
  background: $color-bg-page;
  border-radius: $radius-md;
  border: 1px solid $color-border;
  line-height: $input-height;
  font-size: 28rpx;
  color: $color-text-primary;
}
.readonly { color: $color-text-secondary; }
.profile-hint { text-align: center; color: $color-text-muted; padding: 48rpx 0; }
</style>
