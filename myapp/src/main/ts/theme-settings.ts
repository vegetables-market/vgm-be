// export enum ThemeMode {
//   LIGHT = 'light',
//   DARK = 'dark',
// }
//
// export enum ThemeType {
//   DEFAULT = 'default',
//   BLUE = 'blue',
//   RED = 'red',
// }
//
// //ダークモード切り替え
// export function toggleColorMode(): void {
//   const isDark = document.body.classList.contains('theme-dark');
//
//   document.body.classList.remove('thene-light', 'theme-dark');
//
//   if (isDark) {
//     document.body.classList.add('theme-light');
//     localStorage.setItem('theme-mode', ThemeMode.LIGHT);
//   } else {
//     document.body.classList.add('theme-dark');
//     localStorage.setItem('theme-mode', ThemeMode.DARK);
//   }
// }
//
// export function setThemeColor(color: ThemeColor): void {
//   document.body.classList.remove('theme-default', 'theme-blue', 'theme-red');
//   document.body.classList.add(`theme-${color}`);
//   localStorage.setItem('theme-color', color);
// }
//
// export function initTheme(): void {
//   const baseToggle = document.getElementById('toggleColorMode');
//   const blueToggle = document.getElementById('toggleBlueTheme');
//   const redToggle = document.getElementById('toggleRedTheme');
//
//   baseToggle?.addEventListener('click', toggleColorMode);
//   blueToggle?.addEventListener('click', () => toggleSubTheme(ThemeType.BLUE));
//   redToggle?.addEventListener('click', () => toggleSubTheme(ThemeType.RED));
// }
