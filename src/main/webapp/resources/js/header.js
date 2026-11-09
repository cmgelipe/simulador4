document.addEventListener('DOMContentLoaded', function () {
    var header = document.getElementById('appHeader');
    if (!header) {
        return;
    }
    function onScroll() {
        if (window.scrollY > 8) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    }
    window.addEventListener('scroll', onScroll);
    onScroll();
});
