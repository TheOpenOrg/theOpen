<template>
  <div class="server-list-container">
    <div class="country-list">
      <div class="server-grid">
        <div
          v-for="item in countries"
          :key="item.country.id"
          class="server-card"
          :class="{ selected: selectable && selectedCountry?.id === item.country.id }"
          @click="selectable && $emit('select', item.country)"
        >
          <div class="server-info">
            <div class="flex items-center gap-2">
              <img alt="flag" src="https://primefaces.org/cdn/primevue/images/flag/flag_placeholder.png" :class="`flag flag-${item.country?.code}`" style="width: 32px" />
              <span class="server-country">{{ item.country?.nameRu || item.country?.name || 'Неизвестно' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { serverApi } from '../services/api';

export default {
  name: 'ServerList',
  props: {
    selectable: {
      type: Boolean,
      default: false
    },
    selectedCountry: {
      type: [String, Number, Object, null],
      default: null
    }
  },
  emits: ['select'],
  data() {
    return {
      countries: [{
        "country": {
          "id": 2,
          "name": "USA",
          "code": "us",
          "nameRu": "США"
        }
      }],
      loading: false,
      error: null
    };
  },
  async mounted() {
    this.loading = true;
    this.error = null;
    try {
      this.countries = await serverApi.getAvailableCountries();
    } catch (e) {
      // eslint-disable-next-line no-console
      console.error('Ошибка загрузки списка стран', e);
      this.error = 'Не удалось загрузить список стран';
    } finally {
      this.loading = false;
    }
  },
};
</script>

<style scoped>
.server-list-container {
  min-height: auto;
  padding: 20px 0;
  font-family: 'Inter', Arial, sans-serif;
}
.title {
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 24px;
  letter-spacing: 0.02em;
}
.country-list {
  max-width: 95%;
  margin: 0 auto;
}
@media (min-width: 768px) {
  .country-list {
    max-width: 90%;
  }
}
@media (min-width: 1024px) {
  .country-list {
    max-width: 1000px;
  }
}
.server-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 12px;
}
@media (min-width: 480px) {
  .server-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (min-width: 768px) {
  .server-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}
@media (min-width: 1024px) {
  .server-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }
}
.server-card {
  background: #f8fbfd;
  border-radius: 12px;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.05);
  padding: 16px;
  display: flex;
  flex-direction: column;
  transition: all 0.2s ease;
  cursor: pointer;
  border: 1px solid #e8eef2;
  height: 100%;
}
.server-card:hover {
  box-shadow: 0 4px 12px 0 rgba(0,0,0,0.1);
  transform: translateY(-2px);
}
.server-card.selected {
  background: #e6f7f4;
  border-color: #00e0c6;
}
.server-info {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
  flex-grow: 1;
}
.server-country {
  font-size: 1rem;
  color: #1a2a3a;
  font-weight: 500;
}
.server-properties {
  margin-top: 12px;
}
.server-property {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.85rem;
  color: #4a5a6a;
}
.property-name {
  margin-left: 4px;
}
.server-property:before {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #00e0c6;
  border-radius: 50%;
  margin-right: 8px;
}
.server-footer {
  margin-top: auto;
}
.select-button {
  width: 100%;
  padding: 8px 12px;
  background: #00e0c6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}
.select-button:hover {
  background: #00c4ad;
}
.select-button.disabled {
  background: #6ed7c6;
  cursor: default;
}

</style>
