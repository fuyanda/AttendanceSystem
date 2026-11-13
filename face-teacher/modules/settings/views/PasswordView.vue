<template>
  <PageContainer>
    <NavPageHeader title="修改密码" show-back @back="goBack" />
    <PageSection card>
      <FormField label="原密码">
        <input v-model="form.oldPassword" class="app-input" password placeholder="请输入原密码" />
      </FormField>
      <FormField label="新密码">
        <input v-model="form.newPassword" class="app-input" password placeholder="至少 5 位" />
      </FormField>
      <FormField label="确认新密码">
        <input v-model="form.confirmPassword" class="app-input" password placeholder="再次输入新密码" />
      </FormField>
      <AppButton :loading="saving" @click="submitPassword">确认修改</AppButton>
    </PageSection>
  </PageContainer>
</template>
<script setup>
import PageContainer from '@/shared/components/layout/PageContainer.vue'
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PageSection from '@/shared/components/layout/PageSection.vue'
import AppButton from '@/shared/components/ui/AppButton.vue'
import FormField from '@/shared/components/ui/FormField.vue'
import { usePassword } from '@/modules/settings/composables/usePassword.js'
const { saving, form, goBack, submitPassword } = usePassword()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.app-input {
  height: $input-height;
  padding: 0 $space-24;
  background: $color-bg-page;
  border-radius: $radius-md;
  border: 1px solid $color-border;
  font-size: 28rpx;
}
</style>
