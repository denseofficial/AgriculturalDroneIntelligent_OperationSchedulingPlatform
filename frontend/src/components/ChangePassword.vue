<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({ passwordForm: { type: Object, required: true } })
const emit = defineEmits(['save', 'cancel'])
</script>

<template>
  <form class="task-form" @submit.prevent="$emit('save')">
    <label>旧密码<input v-model="passwordForm.oldPassword" type="password" /></label>
    <label>新密码<input v-model="passwordForm.newPassword" required type="password" /></label>
    <div class="form-actions">
      <button class="secondary" type="submit">提交</button>
      <button class="ghost" type="button" @click="$emit('cancel')">关闭</button>
    </div>
  </form>
</template>

