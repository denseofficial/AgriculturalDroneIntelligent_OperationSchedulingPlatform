<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({ users: { type: Array, default: () => [] } })
const emit = defineEmits(['edit', 'delete', 'change-password'])
</script>

<template>
  <div class="table-wrap">
    <table>
      <thead><tr><th>用户名</th><th>真实姓名</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.username }}</td>
          <td>{{ user.realName }}</td>
          <td>{{ user.role }}</td>
          <td><span class="tag">{{ user.status }}</span></td>
          <td>
            <button class="link-btn" @click="$emit('edit', user)">编辑</button>
            <button class="link-btn" @click="$emit('change-password', user)">修改密码</button>
            <button class="danger-btn" @click="$emit('delete', user.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
    <p v-if="!users.length" class="empty">暂无用户数据</p>
  </div>
</template>

