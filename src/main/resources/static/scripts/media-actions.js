(() => {
    'use strict'

    function onButtonClick(btn) {
        document.querySelectorAll('.media-action-button')
                .forEach(btn2 => {
                    btn2.setAttribute('disabled', 'disabled')
                })
    }

    function registerButton(btn) {
        btn.addEventListener('click', () => onButtonClick(btn))
    }

    function documentLoaded() {
        document.querySelectorAll('.media-action-button')
                .forEach(btn => registerButton(btn))
    }

    window.addEventListener('DOMContentLoaded', () => documentLoaded())
})()
