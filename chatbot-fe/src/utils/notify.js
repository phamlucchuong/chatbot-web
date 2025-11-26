export function showToast(message, type = 'success', timeout = 3500) {
  if (typeof document === 'undefined') return

  let container = document.querySelector('.toast-container')
  if (!container) {
    container = document.createElement('div')
    container.className = 'toast-container'
    document.body.appendChild(container)
  }

  const el = document.createElement('div')
  el.className = `toast ${type}`
  el.textContent = message
  container.appendChild(el)

  // enter animation
  el.style.opacity = '0'
  el.style.transform = 'translateY(-6px)'
  requestAnimationFrame(() => {
    el.style.transition = 'opacity 200ms ease, transform 200ms ease'
    el.style.opacity = '1'
    el.style.transform = 'translateY(0)'
  })

  const id = setTimeout(() => {
    // exit
    el.style.opacity = '0'
    el.style.transform = 'translateY(-6px)'
    setTimeout(() => {
      try { container.removeChild(el) } catch (e) {}
      if (container.childElementCount === 0) {
        try { document.body.removeChild(container) } catch (e) {}
      }
    }, 220)
    clearTimeout(id)
  }, timeout)

  return () => {
    // manual remove
    try { container.removeChild(el) } catch (e) {}
  }
}
