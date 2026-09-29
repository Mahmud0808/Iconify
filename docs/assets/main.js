const REPO = "Mahmud0808/Iconify";
const CACHE_KEY = "iconify-gh-stats";
const TTL = 60 * 60 * 1000;
const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)");

function formatCount(n) {
	if (n >= 1000) return `${(n / 1000).toFixed(n >= 10000 ? 0 : 1)}k`;
	return `${n}`;
}

function renderStats({ stars, version }) {
	if (stars) {
		for (const el of document.querySelectorAll("[data-stat='stars']")) {
			el.textContent = formatCount(stars);
			el.hidden = false;
		}
	}
	if (version) {
		document.querySelector("[data-stat='version']").textContent = version;
		document.querySelector("[data-stat-row]").hidden = false;
		document.querySelector("[data-download-label]").textContent =
			`Download ${version}`;
	}
}

function readCache() {
	try {
		return JSON.parse(localStorage.getItem(CACHE_KEY));
	} catch {
		return null;
	}
}

async function loadStats() {
	const cached = readCache();
	if (cached && Date.now() - cached.at < TTL) {
		renderStats(cached.data);
		return;
	}

	try {
		const [repoRes, releaseRes] = await Promise.all([
			fetch(`https://api.github.com/repos/${REPO}`),
			fetch(`https://api.github.com/repos/${REPO}/releases/latest`),
		]);
		if (!repoRes.ok) throw new Error(`repo ${repoRes.status}`);
		const repo = await repoRes.json();
		const release = releaseRes.ok ? await releaseRes.json() : null;
		const data = {
			stars: repo.stargazers_count,
			version: release?.tag_name ?? null,
		};
		renderStats(data);
		try {
			localStorage.setItem(
				CACHE_KEY,
				JSON.stringify({ at: Date.now(), data }),
			);
		} catch {}
	} catch {
		if (cached?.data) renderStats(cached.data);
	}
}

function startClock() {
	const clocks = document.querySelectorAll("[data-clock]");
	const format = new Intl.DateTimeFormat(undefined, {
		hour: "numeric",
		minute: "2-digit",
	});
	const tick = () => {
		const text = format
			.formatToParts(new Date())
			.filter((p) => p.type !== "dayPeriod")
			.map((p) => p.value)
			.join("")
			.trim();
		clocks.forEach((el) => (el.textContent = text));
	};
	tick();
	setTimeout(
		() => {
			tick();
			setInterval(tick, 60_000);
		},
		60_000 - (Date.now() % 60_000),
	);
}

function stylesFor(group) {
	return [...document.querySelectorAll(`.glyph[data-group='${group}']`)].map(
		(btn) => ({
			name: btn.dataset.name,
			svg: btn.querySelector("svg, .batt"),
		}),
	);
}

function swap(slot, svg, label, name) {
	if (label) label.textContent = name;
	const incoming = svg.cloneNode(true);

	if (reduceMotion.matches) {
		slot.replaceChildren(incoming);
		return;
	}

	for (const stale of slot.querySelectorAll(".is-leaving")) stale.remove();
	const outgoing = slot.firstElementChild;
	slot.append(incoming);

	const easing = "cubic-bezier(0.16, 1, 0.3, 1)";
	if (outgoing) {
		outgoing.classList.add("is-leaving");
		outgoing
			.animate(
				{
					transform: ["none", "translateY(-45%) scale(0.8)"],
					opacity: [1, 0],
				},
				{ duration: 380, easing, fill: "forwards" },
			)
			.finished.then(
				() => outgoing.remove(),
				() => {},
			);
	}
	incoming.animate(
		{
			transform: ["translateY(45%) scale(0.8)", "none"],
			opacity: [0, 1],
		},
		{ duration: 520, delay: 70, easing, fill: "backwards" },
	);
}

function startPoster() {
	const poster = document.querySelector(".poster");
	const toggle = document.querySelector("[data-poster-toggle]");
	if (!poster || reduceMotion.matches) return;

	const groups = ["wifi", "cell", "battery"].map((group) => {
		const styles = stylesFor(group);
		const label = poster.querySelector(`[data-slot-name='${group}']`);
		return {
			styles,
			label,
			slot: poster.querySelector(`[data-slot='${group}']`),
			index: Math.max(
				0,
				styles.findIndex((s) => s.name === label.textContent.trim()),
			),
		};
	});

	let paused = false;
	let visible = true;
	let turn = 0;

	setInterval(() => {
		if (paused || !visible || document.hidden) return;
		const g = groups[turn % groups.length];
		g.index = (g.index + 1) % g.styles.length;
		const next = g.styles[g.index];
		swap(g.slot, next.svg, g.label, next.name);
		turn++;
	}, 1400);

	new IntersectionObserver(([entry]) => {
		visible = entry.isIntersecting;
	}).observe(poster);

	toggle.hidden = false;
	toggle.addEventListener("click", () => {
		paused = !paused;
		toggle.setAttribute("aria-pressed", String(paused));
		toggle.textContent = paused ? "Play" : "Pause";
	});
}

function startPicker() {
	for (const btn of document.querySelectorAll(".glyph")) {
		btn.addEventListener("click", () => {
			const group = btn.dataset.group;
			for (const other of document.querySelectorAll(
				`.glyph[data-group='${group}']`,
			)) {
				other.setAttribute("aria-pressed", String(other === btn));
			}
			swap(
				document.querySelector(`[data-pick='${group}']`),
				btn.querySelector("svg, .batt"),
				document.querySelector(`[data-pick-name='${group}']`),
				btn.dataset.name,
			);
		});
	}
}

function startMap() {
	for (const item of document.querySelectorAll("[data-area]")) {
		const n = item.dataset.area;
		const targets = document.querySelectorAll(
			`[data-region='${n}'], [data-mark='${n}']`,
		);
		const light = (on) => {
			item.classList.toggle("is-lit", on);
			targets.forEach((t) => t.classList.toggle("is-lit", on));
		};
		item.addEventListener("pointerenter", () => light(true));
		item.addEventListener("pointerleave", () => light(false));
	}
}

function startStrip() {
	const strip = document.querySelector(".shot-strip");
	const nav = document.querySelector(".strip-nav");
	if (!strip || !nav) return;
	const [prev, next] = nav.querySelectorAll("button");

	const update = () => {
		const max = strip.scrollWidth - strip.clientWidth;
		nav.hidden = max <= 1;
		prev.disabled = strip.scrollLeft <= 1;
		next.disabled = strip.scrollLeft >= max - 1;
	};

	for (const btn of [prev, next]) {
		btn.addEventListener("click", () => {
			strip.scrollBy({
				left: Number(btn.dataset.strip) * strip.clientWidth,
				behavior: reduceMotion.matches ? "auto" : "smooth",
			});
		});
	}

	strip.addEventListener("scroll", update, { passive: true });
	new ResizeObserver(update).observe(strip);
	update();
}

function startFaq() {
	const easing = "cubic-bezier(0.16, 1, 0.3, 1)";
	for (const details of document.querySelectorAll(".faq details")) {
		const summary = details.querySelector("summary");
		const answer = details.querySelector(".faq-a");
		let running = null;

		summary.addEventListener("click", (e) => {
			if (reduceMotion.matches) return;
			e.preventDefault();
			running?.cancel();

			const closing =
				details.open && !details.classList.contains("is-closing");
			const from = `${answer.getBoundingClientRect().height}px`;

			if (closing) {
				details.classList.add("is-closing");
				running = answer.animate(
					{ height: [from, "0px"], opacity: [1, 0] },
					{ duration: 240, easing },
				);
				running.onfinish = () => {
					details.open = false;
					details.classList.remove("is-closing");
					running = null;
				};
			} else {
				details.classList.remove("is-closing");
				details.open = true;
				const to = `${answer.scrollHeight}px`;
				running = answer.animate(
					{ height: [from, to], opacity: [0, 1] },
					{ duration: 320, easing },
				);
				running.onfinish = () => {
					running = null;
				};
			}
		});
	}
}

function startLightbox() {
	const lightbox = document.getElementById("lightbox");
	if (!lightbox || typeof lightbox.showModal !== "function") return;
	const img = lightbox.querySelector("img");
	const caption = lightbox.querySelector("figcaption");

	for (const btn of document.querySelectorAll(".shot-zoom")) {
		btn.addEventListener("click", () => {
			const shot = btn.querySelector("img");
			img.src = shot.currentSrc || shot.src;
			img.alt = shot.alt;
			caption.textContent =
				btn.closest(".shot")?.querySelector("p")?.textContent ?? "";
			lightbox.showModal();
		});
	}

	lightbox.addEventListener("click", (e) => {
		if (e.target === lightbox) lightbox.close();
	});

	lightbox.addEventListener("close", () => img.removeAttribute("src"));
}

loadStats();
startClock();
startPoster();
startPicker();
startMap();
startStrip();
startFaq();
startLightbox();
