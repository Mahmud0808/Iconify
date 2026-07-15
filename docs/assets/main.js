const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

const revealables = document.querySelectorAll(".reveal");
if ("IntersectionObserver" in window && !reduceMotion.matches) {
  const observer = new IntersectionObserver(
    (entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add("revealed");
          observer.unobserve(entry.target);
        }
      }
    },
    { threshold: 0.12, rootMargin: "0px 0px -40px 0px" }
  );
  revealables.forEach((el) => observer.observe(el));
} else {
  revealables.forEach((el) => el.classList.add("revealed"));
}

class Accordion {
  constructor(details) {
    this.details = details;
    this.summary = details.querySelector("summary");
    this.animation = null;
    this.summary.addEventListener("click", (e) => this.onClick(e));
  }

  onClick(e) {
    e.preventDefault();
    if (reduceMotion.matches) {
      this.details.open = !this.details.open;
      return;
    }
    this.details.style.overflow = "hidden";
    if (this.details.open) {
      this.shrink();
    } else {
      this.expand();
    }
  }

  shrink() {
    const startHeight = this.details.offsetHeight;
    const endHeight = this.summary.offsetHeight;
    this.animate(startHeight, endHeight, false);
  }

  expand() {
    this.details.open = true;
    const startHeight = this.summary.offsetHeight;
    const endHeight = this.details.offsetHeight;
    this.animate(startHeight, endHeight, true);
  }

  animate(startHeight, endHeight, opening) {
    this.animation?.cancel();
    const body = this.details.querySelector(".faq-body");
    const bodyPad = getComputedStyle(body).paddingBottom;
    this.animation = this.details.animate(
      { height: [`${startHeight}px`, `${endHeight}px`] },
      { duration: 300, easing: "cubic-bezier(0.22, 0.9, 0.3, 1)" }
    );
    body.animate(
      {
        paddingBottom: opening ? ["0px", bodyPad] : [bodyPad, "0px"],
        opacity: opening ? [0, 1] : [1, 0]
      },
      { duration: 300, easing: "cubic-bezier(0.22, 0.9, 0.3, 1)" }
    );
    this.animation.onfinish = () => this.onFinish(opening);
    this.animation.oncancel = () => {
      this.animation = null;
    };
  }

  onFinish(open) {
    this.details.open = open;
    this.details.style.overflow = "";
    this.animation = null;
  }
}

document.querySelectorAll(".faq-item").forEach((d) => new Accordion(d));

const navLinks = [...document.querySelectorAll(".pill-nav a[href^='#']")];
const targets = navLinks
  .map((link) => document.querySelector(link.hash === "#top" ? ".hero" : link.hash))
  .filter(Boolean);

const sectionObserver = new IntersectionObserver(
  (entries) => {
    for (const entry of entries) {
      if (!entry.isIntersecting) continue;
      const id = entry.target.id || "top";
      navLinks.forEach((link) =>
        link.classList.toggle("active", link.hash === `#${id}`)
      );
    }
  },
  { rootMargin: "-40% 0px -55% 0px" }
);
targets.forEach((t) => sectionObserver.observe(t));
