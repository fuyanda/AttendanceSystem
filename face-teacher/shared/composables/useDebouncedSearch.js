import { computed, ref } from 'vue'

/**
 * ������������Ч�ؼ��ʷ��룬��������󴥷� applyFn��
 * @param {(keyword: string) => void} applyFn �ؼ��ʱ仯��ִ�У��� trim��
 * @param {number} [delayMs=300]
 */
export function useDebouncedSearch(applyFn, delayMs = 300) {
  const keyword = ref('')
  const appliedKeyword = ref('')
  const isSearching = computed(() => !!appliedKeyword.value.trim())
  let timer = null

  function apply(value) {
    const next = (value ?? keyword.value).trim()
    appliedKeyword.value = next
    applyFn(next)
  }

  function onSearch(value) {
    if (timer) clearTimeout(timer)
    keyword.value = value ?? ''
    apply(value)
  }

  function onSearchInput(value) {
    keyword.value = value ?? ''
    if (timer) clearTimeout(timer)
    if (!keyword.value.trim()) {
      apply('')
      return
    }
    timer = setTimeout(() => apply(keyword.value), delayMs)
  }

  function resetSearch() {
    if (timer) clearTimeout(timer)
    keyword.value = ''
    appliedKeyword.value = ''
  }

  return {
    keyword,
    appliedKeyword,
    isSearching,
    onSearch,
    onSearchInput,
    resetSearch
  }
}
