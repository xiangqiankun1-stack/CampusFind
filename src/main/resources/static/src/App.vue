<template>
  <router-view />
</template>

<script setup>
// 这里不需要写任何复杂的表格查询和身份判断逻辑！保持纯净！
</script>

<style>
/* 全局基础样式重置，防止页面四周出现莫名其妙的白边 */
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  background-color: #f8fafc;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
}

#app {
  height: 100%;
  width: 100%;
}
</style>