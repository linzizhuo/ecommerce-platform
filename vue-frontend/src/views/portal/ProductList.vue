<template><div>ProductList - Redirect to Home</div></template>
<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
onMounted(() => useRouter().push('/'))
</script>
