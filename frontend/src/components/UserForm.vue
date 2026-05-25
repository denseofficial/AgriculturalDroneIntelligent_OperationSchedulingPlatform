<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({ user: { type: Object, required: true }, passwordForm: { type: Object, required: false } })
const emit = defineEmits(['save', 'cancel'])
</script>

<template>
  <form class="task-form" @submit.prevent="$emit('save')">
    <label>用户名<input v-model="user.username" required /></label>
    <label>真实姓名<input v-model="user.realName" required /></label>
    <label>角色<select v-model="user.role"><option value="ADMIN">管理员</option><option value="OPERATOR">操作员</option></select></label>
    <label>状态<select v-model="user.status"><option value="ACTIVE">激活</option><option value="DISABLED">禁用</option></select></label>
    <label v-if="!user.id">初始密码<input v-model="passwordForm.newPassword" type="password" /></label>
    <div class="form-actions">
      <button class="secondary" type="submit">{{ user.id ? '更新用户' : '创建用户' }}</button>
      <button class="ghost" type="button" @click="$emit('cancel')">关闭</button>
    </div>
  </form>
</template>

