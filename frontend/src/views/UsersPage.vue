<script setup>
import { ref, reactive, onMounted } from 'vue'
import { api } from '../api'
import UsersList from '../components/UsersList.vue'
import UserForm from '../components/UserForm.vue'
import ChangePassword from '../components/ChangePassword.vue'

const users = ref([])
const showUserForm = ref(false)
const showPasswordForm = ref(false)
const userForm = reactive({ id: null, username: '', realName: '', role: 'OPERATOR', status: 'ACTIVE' })
const passwordForm = reactive({ oldPassword: '', newPassword: '' })
const loading = ref(false)
const message = ref('')

const loadUsers = async () => {
  try {
    users.value = await api.users()
  } catch (err) {
    message.value = err.message
  }
}

const openNewUser = () => {
  Object.assign(userForm, { id: null, username: '', realName: '', role: 'OPERATOR', status: 'ACTIVE' })
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  showUserForm.value = true
}

const editUser = (user) => {
  Object.assign(userForm, { id: user.id, username: user.username, realName: user.realName, role: user.role, status: user.status })
  showUserForm.value = true
}

const saveUser = async () => {
  loading.value = true
  try {
    if (userForm.id == null) {
      await api.createUser({ username: userForm.username, password: passwordForm.newPassword || 'changeme', realName: userForm.realName, role: userForm.role, status: userForm.status })
    } else {
      await api.updateUser(userForm.id, { username: userForm.username, realName: userForm.realName, role: userForm.role, status: userForm.status })
    }
    showUserForm.value = false
    await loadUsers()
  } catch (err) {
    message.value = err.message
  } finally {
    loading.value = false
  }
}

const removeUser = async (id) => {
  if (!window.confirm('确认删除该用户？')) return
  await api.deleteUser(id)
  await loadUsers()
}

const openChangePassword = (user) => {
  userForm.id = user.id
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  showPasswordForm.value = true
}

const changePassword = async () => {
  loading.value = true
  try {
    await api.changePassword(userForm.id, { oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword })
    showPasswordForm.value = false
    await loadUsers()
  } catch (err) {
    message.value = err.message
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<template>
  <section v-if="showUserForm" class="panel">
    <div class="section-title">
      <h2>{{ userForm.id ? '编辑用户' : '新增用户' }}</h2>
      <button class="ghost" type="button" @click="showUserForm = false">关闭</button>
    </div>
    <UserForm :user="userForm" :passwordForm="passwordForm" @save="saveUser" @cancel="showUserForm = false" />
  </section>

  <section class="panel">
    <div class="section-title">
      <h2>用户列表</h2>
      <div class="title-actions">
        <span>{{ users.length }} 人</span>
        <button class="secondary" type="button" @click="openNewUser">新增用户</button>
      </div>
    </div>
    <UsersList :users="users" @edit="editUser" @delete="removeUser" @change-password="openChangePassword" />
  </section>

  <section v-if="showPasswordForm" class="panel">
    <div class="section-title"><h2>修改密码</h2><button class="ghost" @click="showPasswordForm = false">关闭</button></div>
    <ChangePassword :passwordForm="passwordForm" @save="changePassword" @cancel="showPasswordForm = false" />
  </section>
</template>

