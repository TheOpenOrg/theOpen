<template>
  <div class="server-list-container">
    <h1 class="title">Выберите сервер</h1>
    <div class="servers">
      <div
        v-for="server in servers"
        :key="server.id"
        class="server-card"
      >
        <h2 class="server-name">{{ server.name }}</h2>
        <p class="server-description">{{ server.description }}</p>
        <ul class="server-benefits">
          <li v-for="(benefit, idx) in server.benefits" :key="idx">
            {{ benefit }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ServerList',
  data() {
    return {
      servers: [],
    };
  },
  async mounted() {
    try {
      const response = await fetch(import.meta.env.VITE_API_URL + '/api/servers/available');
      const data = await response.json();
      // Ожидается, что у сервера есть name, description, benefits (массив)
      this.servers = data;
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('Ошибка загрузки серверов', e);
    }
  },
};
</script>

<style scoped>
.server-list-container {
  min-height: 100vh;
  background: #f7fafd;
  padding: 40px 0;
  font-family: 'Inter', Arial, sans-serif;
}
.title {
  text-align: center;
  font-size: 2.5rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 32px;
  letter-spacing: 0.02em;
}
.servers {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 32px;
}
.server-card {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 24px 0 rgba(0,0,0,0.07);
  padding: 32px 28px;
  width: 340px;
  transition: box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.server-card:hover {
  box-shadow: 0 8px 32px 0 rgba(0,0,0,0.13);
}
.server-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #0a1a2f;
  margin-bottom: 10px;
}
.server-description {
  font-size: 1rem;
  color: #4a5a6a;
  margin-bottom: 16px;
}
.server-benefits {
  list-style: none;
  padding: 0;
  margin: 0;
}
.server-benefits li {
  font-size: 0.98rem;
  color: #1a2a3a;
  margin-bottom: 7px;
  padding-left: 1.2em;
  position: relative;
}
.server-benefits li:before {
  content: '';
  display: inline-block;
  width: 7px;
  height: 7px;
  background: #00e0c6;
  border-radius: 50%;
  position: absolute;
  left: 0;
  top: 7px;
}
</style>

