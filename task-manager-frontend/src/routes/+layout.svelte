<script lang="ts">
	import '../app.css';
	import { Navbar, NavBrand, NavLi, NavUl, NavHamburger } from 'flowbite-svelte';

	let { children } = $props();
</script>

<Navbar>
	<NavBrand>
		<p>Task Manager</p>
	</NavBrand>
	<div class="order-2">
		<NavHamburger />
	</div>

	<NavUl class="order-1">
		<NavLi href="/">Home</NavLi>
		<NavLi href="/login">Login</NavLi>
		<NavLi href="/register">Register</NavLi>
	</NavUl>
</Navbar>

{@render children()}
