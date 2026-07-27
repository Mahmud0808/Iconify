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
		{ threshold: 0.12, rootMargin: "0px 0px -40px 0px" },
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
			{ duration: 300, easing: "cubic-bezier(0.22, 0.9, 0.3, 1)" },
		);
		body.animate(
			{
				paddingBottom: opening ? ["0px", bodyPad] : [bodyPad, "0px"],
				opacity: opening ? [0, 1] : [1, 0],
			},
			{ duration: 300, easing: "cubic-bezier(0.22, 0.9, 0.3, 1)" },
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

// ---- icon pack marquees: fill each track to viewport width, then clone for a gapless loop ----
for (const marquee of document.querySelectorAll(".marquee")) {
	const [track, clone] = marquee.querySelectorAll(".marquee-track");
	if (!track || !clone) continue;
	const items = [...track.children];
	for (let i = 0; track.scrollWidth < marquee.clientWidth && i < 10; i++) {
		for (const item of items) track.appendChild(item.cloneNode(true));
	}
	clone.innerHTML = track.innerHTML;

	// duration scales with track width so every row moves at the same speed
	const duration = `${track.scrollWidth / 40}s`;
	track.style.animationDuration = duration;
	clone.style.animationDuration = duration;
}

// ---- liquid-glass nav: lens-style edge refraction.
// Needs backdrop-filter: url(), so it's enabled only where that works (Chromium);
// other browsers keep the plain frosted-blur look. ----
(() => {
	const nav = document.querySelector(".pill-nav");
	const map = document.getElementById("lg-map");
	if (!nav || !map) return;

	const isWebkit =
		/Safari/.test(navigator.userAgent) &&
		!/Chrome/.test(navigator.userAgent);
	const isFirefox = /Firefox/.test(navigator.userAgent);
	const probe = document.createElement("div");
	probe.style.backdropFilter = "url(#liquid-glass)";
	if (isWebkit || isFirefox || probe.style.backdropFilter === "") return;

	// displacement map: R drives x, G drives y. 50% gray (0.5) means "no shift",
	// so both gradients hold 0.5 through the middle and ramp smoothly only near
	// the rim — a continuous profile (no steps), otherwise the backdrop doubles
	const updateMap = () => {
		const { width, height } = nav.getBoundingClientRect();
		if (!width || !height) return;
		// rasterize at device resolution — at fractional/high DPR the map is
		// otherwise misaligned with the backdrop and parts of the pill stay sharp
		const dpr = window.devicePixelRatio || 1;
		// same physical rim width on every side, so all four edges bend equally
		const ramp = Math.min(width, height) * 0.35;
		const rx = ((ramp / width) * 100).toFixed(2);
		const ry = ((ramp / height) * 100).toFixed(2);
		const svg =
			`<svg width="${width * dpr}" height="${height * dpr}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">` +
			`<defs>` +
			`<linearGradient id="lg-x" x1="0%" y1="0%" x2="100%" y2="0%">` +
			`<stop offset="0%" stop-color="#ff0000"/>` +
			`<stop offset="${rx}%" stop-color="#800000"/>` +
			`<stop offset="${100 - rx}%" stop-color="#800000"/>` +
			`<stop offset="100%" stop-color="#000000"/>` +
			`</linearGradient>` +
			`<linearGradient id="lg-y" x1="0%" y1="0%" x2="0%" y2="100%">` +
			`<stop offset="0%" stop-color="#00ff00"/>` +
			`<stop offset="${ry}%" stop-color="#008000"/>` +
			`<stop offset="${100 - ry}%" stop-color="#008000"/>` +
			`<stop offset="100%" stop-color="#000000"/>` +
			`</linearGradient>` +
			`</defs>` +
			`<rect width="${width}" height="${height}" fill="url(#lg-x)"/>` +
			`<rect width="${width}" height="${height}" fill="url(#lg-y)" style="mix-blend-mode:screen"/>` +
			`</svg>`;
		// pin the map to the element box in user units — the filter region is
		// larger than the element, so percentage sizing would misalign it
		map.setAttribute("x", "0");
		map.setAttribute("y", "0");
		map.setAttribute("width", `${width}`);
		map.setAttribute("height", `${height}`);
		map.setAttribute(
			"href",
			`data:image/svg+xml,${encodeURIComponent(svg)}`,
		);
	};

	// single displacement pass: subtle lens bend at the rim, no color fringing
	document.getElementById("lg-lens")?.setAttribute("scale", "-35");

	updateMap();
	new ResizeObserver(updateMap).observe(nav);
	nav.classList.add("glass-active");
})();

// ---- live GitHub stats with count-up ----
const statsEl = document.getElementById("gh-stats");

// hold the count-up until the stats row starts fading in, so the
// placeholder is never visible and the count runs during the reveal
const statsReady = new Promise((resolve) => {
	if (!statsEl || reduceMotion.matches) {
		resolve();
		return;
	}
	let settled = false;
	const settle = () => {
		if (!settled) {
			settled = true;
			resolve();
		}
	};
	statsEl.addEventListener("animationstart", settle, { once: true });
	setTimeout(settle, 1500);
});

function formatStat(n) {
	if (n >= 1000) return `${(n / 1000).toFixed(n >= 10000 ? 0 : 1)}k`;
	return `${n}`;
}

function countUp(el, target) {
	if (reduceMotion.matches) {
		el.textContent = formatStat(target);
		return;
	}
	// reserve the final width up front so the row doesn't shift while counting
	el.style.minWidth = `${formatStat(target).length}ch`;
	const duration = 1200;
	const start = performance.now();
	const tick = (now) => {
		const t = Math.min((now - start) / duration, 1);
		const eased = 1 - Math.pow(1 - t, 4);
		el.textContent = formatStat(Math.round(target * eased));
		if (t < 1) requestAnimationFrame(tick);
	};
	requestAnimationFrame(tick);
}

function renderStats({ stars, forks, version }) {
	const starsEl = statsEl.querySelector("[data-stat='stars']");
	const forksEl = statsEl.querySelector("[data-stat='forks']");
	const versionEl = statsEl.querySelector("[data-stat='version']");
	statsReady.then(() => {
		if (stars) countUp(starsEl, stars);
		if (forks) countUp(forksEl, forks);
		if (version) versionEl.textContent = version;
	});
}

async function loadStats() {
	if (!statsEl) return;
	const CACHE_KEY = "iconify-gh-stats";
	const TTL = 60 * 60 * 1000;
	try {
		const cached = JSON.parse(localStorage.getItem(CACHE_KEY));
		if (cached && Date.now() - cached.at < TTL) {
			renderStats(cached.data);
			return;
		}
	} catch {
		/* corrupt cache — refetch */
	}

	try {
		const [repoRes, releaseRes] = await Promise.all([
			fetch("https://api.github.com/repos/Mahmud0808/Iconify"),
			fetch(
				"https://api.github.com/repos/Mahmud0808/Iconify/releases/latest",
			),
		]);
		if (!repoRes.ok) throw new Error(`repo ${repoRes.status}`);
		const repo = await repoRes.json();
		const release = releaseRes.ok ? await releaseRes.json() : null;
		const data = {
			stars: repo.stargazers_count,
			forks: repo.forks_count,
			version: release?.tag_name ?? null,
		};
		renderStats(data);
		try {
			localStorage.setItem(
				CACHE_KEY,
				JSON.stringify({ at: Date.now(), data }),
			);
		} catch {
			/* storage full or blocked — fine */
		}
	} catch {
		// rate-limited or offline: try stale cache, else hide the strip
		try {
			const stale = JSON.parse(localStorage.getItem(CACHE_KEY));
			if (stale?.data) {
				renderStats(stale.data);
				return;
			}
		} catch {
			/* ignore */
		}
		statsEl.hidden = true;
	}
}
loadStats();

// ---- screenshot lightbox ----
const lightbox = document.getElementById("lightbox");
if (lightbox && typeof lightbox.showModal === "function") {
	const lightboxImg = lightbox.querySelector("img");
	const lightboxCaption = lightbox.querySelector("figcaption");

	for (const btn of document.querySelectorAll(".shot-zoom")) {
		btn.addEventListener("click", () => {
			const img = btn.querySelector("img");
			const caption = btn
				.closest(".shot-card")
				?.querySelector("figcaption");
			lightboxImg.src = img.currentSrc || img.src;
			lightboxImg.alt = img.alt;
			lightboxCaption.textContent = caption?.textContent ?? "";
			lightbox.showModal();
		});
	}

	lightbox
		.querySelector(".lightbox-close")
		.addEventListener("click", () => lightbox.close());

	// click on backdrop closes
	lightbox.addEventListener("click", (e) => {
		if (e.target === lightbox) lightbox.close();
	});

	lightbox.addEventListener("close", () => {
		lightboxImg.src = "";
	});
}

const navLinks = [...document.querySelectorAll(".pill-nav a[href^='#']")];
const targets = navLinks
	.map((link) =>
		document.querySelector(link.hash === "#top" ? ".hero" : link.hash),
	)
	.filter(Boolean);

const sectionObserver = new IntersectionObserver(
	(entries) => {
		for (const entry of entries) {
			if (!entry.isIntersecting) continue;
			const id = entry.target.id || "top";
			navLinks.forEach((link) =>
				link.classList.toggle("active", link.hash === `#${id}`),
			);
		}
	},
	{ rootMargin: "-40% 0px -55% 0px" },
);
targets.forEach((t) => sectionObserver.observe(t));
