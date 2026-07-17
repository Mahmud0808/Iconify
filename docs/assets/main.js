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

// ---- icon pack marquees: fill each track to viewport width, then clone for a seamless loop ----
for (const marquee of document.querySelectorAll(".marquee")) {
	const [track, clone] = marquee.querySelectorAll(".marquee-track");
	if (!track || !clone) continue;
	const items = [...track.children];
	for (let i = 0; track.scrollWidth < marquee.clientWidth && i < 10; i++) {
		for (const item of items) track.appendChild(item.cloneNode(true));
	}
	clone.innerHTML = track.innerHTML;
}

// ---- cursor spotlight on feature cards ----
if (window.matchMedia("(hover: hover)").matches) {
	for (const card of document.querySelectorAll(".feature-card")) {
		card.addEventListener("mousemove", (e) => {
			const rect = card.getBoundingClientRect();
			card.style.setProperty("--mx", `${e.clientX - rect.left}px`);
			card.style.setProperty("--my", `${e.clientY - rect.top}px`);
		});
	}
}

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
