# A Brief History of DIY Bitcoin Hardware Signing Devices

**Author:** 九神二号  
**English translation:** ChatGPT  
**Original publication date:** September 20, 2026

> **Original Chinese article:** https://github.com/kdmukai/article-diy-signing-device-summit/blob/main/article.md

## The Starting Point: If Trezor's Code Is Already Open Source, Why Can't You Build One Yourself?

The story begins with a question that seems almost obvious, yet surprisingly few people had actually tried to put into practice.

In February 2018, a developer calling himself **“pitrezor”** published a tutorial on his blog with a title that almost seemed to challenge the entire hardware-wallet industry:

If the firmware for the Trezor One is already completely open source, why shouldn't an ordinary person be able to build one themselves using a Raspberry Pi costing only a few dozen dollars, reproducing a Bitcoin signing device with equivalent functionality?

That was the origin of the **PiTrezor** project.

Its approach was highly “engineer-like”: rather than redesigning the signing logic from scratch, it took the official open-source Trezor One code and wrapped it in a thin compatibility layer, porting code originally intended to run on Trezor's dedicated hardware onto the Linux environment of a **Raspberry Pi Zero** or **Pi 4**.

The hardware requirements were extremely minimal: a Raspberry Pi, a 0.96-inch OLED display — or simply HDMI output — plus one or two buttons.

The project claimed 100% compatibility with Trezor's official web wallet and could be used to sign real Bitcoin transactions directly. It also made use of the Raspberry Pi's built-in hardware random-number generator to improve the security of key generation, while startup time was optimized to roughly five seconds.

From the beginning, PiTrezor never pretended to be a polished, “plug-and-play” consumer product.

When the third-party review organization **WalletScrutiny** analyzed it, it explicitly categorized PiTrezor as a **“DIY Project.”** It also highlighted an important issue: the project distributed precompiled firmware, meaning users had to verify for themselves whether the distributed binaries could actually be reproduced entirely from the publicly available source code.

This would later become a common issue facing almost every DIY signing-device project:

Open-source code is not the final destination. **Reproducible builds** are the final mile required to make the promise of open source truly meaningful.

## BitBoy and Bowser: Educational Toys Become Real Devices

If PiTrezor demonstrated that it was possible to reproduce someone else's open-source design, the next two projects demonstrated that it was equally possible to design a DIY signing device from scratch.

Interestingly, their original motivation was not primarily anxiety about security. It was education and experimentation.

In September 2019, **Justin Moon**, an instructor at the Austin-based Bitcoin training program **BUIDL Bootcamp**, released **BitBoy**, a homemade hardware wallet built around the **M5Stack**, an ESP32 development platform.

It featured two major characteristics:

- stateless operation; and
- QR-code-based air-gapped communication.

For a time, BitBoy caused quite a stir on Crypto Twitter.

Moon himself openly admitted that it had simply been a fun educational project he created less than a year after becoming involved with Bitcoin.

It was followed in 2020 by **Bowser Wallet**, released by the pseudonymous developer **“arcbtc” — Ben Arc**.

Ben Arc is better known for creating **LNbits**, the open-source Lightning wallet and backend system, as well as for his early contributions to the **Nostr** protocol.

His central philosophy behind DIY hardware was to encourage ordinary people to build things themselves, thereby gaining greater autonomy over the Bitcoin devices they use.

## Specter-DIY and DIY Jade: One a “Mainline Project,” the Other a Side Project

Around the same period as BitBoy, a far more systematic project — and ultimately one of the most influential — began to take shape.

This was no longer merely another weekend hackathon project. It effectively marked the beginning of the broader DIY signing-device movement.

In 2019, the **CryptoAdvance** team, led by physicist **Stepan Snigirev**, released **Specter-DIY**, built around the **STM32 F469I-DISCO** development board.

From the very beginning, the project deliberately committed to openness throughout the entire technology stack.

Specter-DIY used a relatively high-end development board with a touchscreen whose performance was comparable to an iPhone 4. More importantly, it pioneered two features that would later become almost standard across DIY signing devices.

The first was **fully air-gapped QR-code communication**.

The device had no physical USB, Bluetooth, or Wi-Fi connection to an internet-connected computer. Transaction data could therefore be exchanged entirely through QR codes.

The second was a **stateless operating model**.

Private keys were loaded temporarily into the device only when needed. Once the operation was complete, they were permanently erased.

The device itself therefore became little more than a tool, completely decoupled from however the user actually chose to store the private keys.

Its companion desktop wallet, **Specter Desktop**, was also the first coordinator wallet to support entirely QR-based communication, allowing users to freely combine DIY signing devices and commercial hardware wallets when building multisignature setups.

From the very first day of the project, Stepan also extracted all of the underlying Bitcoin cryptographic and transaction logic into a separate reusable software library called **embit**.

At the time, this looked like ordinary software-engineering practice.

Six years later, however, embit had become something close to the common ancestor of the entire DIY ecosystem.

Both **SeedSigner** and **Krux** would later be built on the same embit library.

Although the three projects have different philosophies, user experiences, and target audiences, their reliance on the same underlying core code has led their communities to regard them almost as members of the same extended family.

In January 2021, a more unexpected participant entered the scene: long-established Bitcoin infrastructure company **Blockstream** released the **Blockstream Jade**.

Although Jade could be purchased as a finished commercial product, it had DIY characteristics from day one.

The software could also be run on hardware such as the **LILYGO T-Display**, meaning users could purchase their own components and assemble their own devices, thereby reducing certain supply-chain risks.

## SeedSigner: First Drive the Prototype Cost Down to $50, Then Become the “UX Champion”

If Specter-DIY demonstrated that DIY could be technically sophisticated, **SeedSigner** was arguably the first project to demonstrate that DIY hardware could also be genuinely easy to use.

Behind the project is a developer who has chosen to remain pseudonymous.

In December 2020, with the help of the **embit** library and significant assistance from Stepan himself, he built the first functional prototype using a **Raspberry Pi Zero costing only $5**.

The project quickly attracted additional contributors and began introducing a steady stream of new features.

Many of those features would later be adopted throughout the wider industry as de facto standards.

Examples include:

- the option to derive new key-generation entropy from camera imagery;
- the open **SeedQR** encoding standard; and
- workflows in which users can manually transcribe QR codes by hand.

The project's choice of core hardware was also particularly clever.

SeedSigner deliberately uses the **Raspberry Pi Zero version 1.3**, which has no Wi-Fi or Bluetooth hardware.

This physically eliminates the possibility of private keys being transmitted wirelessly from the device.

Combined with a camera and display, all transaction data is exchanged using animated QR codes shown on the screen.

The seed exists temporarily in memory only while the device is powered on and disappears when power is removed.

Interestingly, SeedSigner's core team has itself acknowledged that among the three major DIY projects, it is technically the **“least hardcore.”**

But that became one of its strengths.

Instead of focusing almost exclusively on technical sophistication, the team invested enormous effort into making the interaction model accessible to newcomers.

As a result, SeedSigner achieved the greatest Twitter visibility and podcast exposure of the three projects.

For a project with no marketing budget and maintained entirely by volunteers, that level of attention is itself a scarce resource.

## Krux: Moving DIY onto Development Boards That Already Exist

SeedSigner demonstrated that the Raspberry Pi approach could work, but the Raspberry Pi still requires users to add a separate display and camera before it becomes a complete device.

**Krux** took a different approach.

Rather than assembling a device around a Raspberry Pi, Krux takes advantage of the already mature ecosystem of **Kendryte K210** development boards.

Devices such as the **M5StickV** and **Maix Amigo** already integrate a camera, display, and buttons into a single enclosure.

In some cases, no physical assembly is required at all. Users can simply buy the device and flash the Krux firmware onto it.

Krux was started by a developer in 2021.

However, much like Satoshi Nakamoto himself, the original developer eventually handed the project entirely over to other contributors and disappeared from active involvement.

Today, the project is led and maintained by **odudex**, and it too is built on the **embit** library originally developed by Stepan.

More recently, the Krux team has become one of the main forces pushing advanced new functionality into embit itself.

## ShieldSigner and Kern: Variants That Grew Naturally Out of the DIY Ecosystem

**ShieldSigner**, created by developer **CryptoGuide** — GitHub username **“3rdIteration”** — is currently one of the most successful forks of SeedSigner.

CryptoGuide added support for **Satochip smart cards** and personally designed a custom expansion board capable of reading those smart cards.

SeedSigner has often been criticized because it contains **no secure element**.

A Satochip smart card, by contrast, effectively functions as a secure element, but lacks a screen through which users can independently verify what they are signing.

Combining the two therefore appears to many people to be a natural fit.

Interestingly, even in this area the real pioneer was once again Specter-DIY, which had already offered optional smart-card support years earlier.

**Kern** — https://github.com/odudex/Kern — is another experimental project, written from scratch in C by Krux maintainer **odudex**.

It is still at an early stage, but expectations around it are already significant.

For example, its QR-code scanning module may eventually be integrated directly into the next generation of SeedSigner, which is expected to use a microcontroller rather than a Raspberry Pi.

That would provide yet another example of code being reused across different DIY signing-device projects.

## November 2025, São Paulo: Three Parallel Paths Finally Converge

Most of the developments described so far emerged independently within each project.

The most significant turning point in the timeline, however, occurred in **November 2025 in São Paulo, Brazil**.

For the first time, core members of the **Specter-DIY, SeedSigner, and Krux** teams gathered together in person.

They were joined by experts from **Satochip** and **Jade DIY**, along with self-custody educators.

For the first time, people who had spent years building parallel technologies finally met one another face-to-face and began a much deeper level of collaboration.

The gathering was organized through the efforts of **Lucas Ferreira**, Executive Director of the Brazilian nonprofit **Vinteum**.

He persuaded all of the key participants, including **Stepan Snigirev**, to attend.

That was itself a significant accomplishment.

After an intense period of development several years earlier, Stepan had come close to burnout and had largely withdrawn from active Bitcoin development. Some people had even wondered whether he would ever return to the community.

Vinteum handled the event's logistics.

Funding came from the **Human Rights Foundation**, and the gathering took place at **Casa 21**, Vinteum's hacker house in São Paulo.

The most important item on the summit agenda was the transfer of stewardship over **embit**, the core library shared by all three projects.

One of Stepan's primary reasons for attending was to formally transfer maintenance responsibility for embit to a new group of co-maintainers.

He no longer wanted to be a bottleneck slowing the progress of the wider ecosystem and understandably wanted to reduce his maintenance burden.

Ultimately, Specter-DIY, SeedSigner, and Krux each nominated one representative to become a co-maintainer of the embit repository.

In symbolic terms, it represented the passing of the torch from one generation of maintainers to the next.

During the summit, each team openly demonstrated its technical work to the others.

The **Specter-DIY** team demonstrated the implementation details of its STM32 secure bootloader.

This mechanism ensures that the device refuses to install unauthorized or unsigned **“evil firmware.”**

For a signing device, this is arguably one of the most important security properties that can be provided.

SeedSigner's volunteer UX designer demonstrated the methodology used to refine the project's user experience, which has already inspired contributors to Krux to experiment with further UX improvements.

The Krux team, meanwhile, designed an encrypted multisignature challenge that required participants to combine several of Krux's unique advanced capabilities in order to solve it, demonstrating the level of sophistication the tool can reach in expert hands.

For years, each project had independently accumulated its own technical advantages.

For the first time, those capabilities were placed on the same table.

And anyone was free to use them without permission or authorization, because they were all free and open-source software.

## Conclusion: Why the DIY Spirit Still Matters

Looking back over the journey from PiTrezor's first attempt in 2018 to **“replicate open-source firmware,”** through to the formal handover of a shared software library between the three teams in São Paulo in 2025, a clear line of reasoning emerges.

The old Bitcoin maxim:

**“Not your keys, not your coins.”**

was taken one step further by the DIY movement.

It is not enough merely to control your private keys.

Ideally, you should also be able to understand, verify, and perhaps even physically assemble the device used to generate and operate those keys.

From PiTrezor's almost self-evident question, to Justin Moon's educational belief that **building a hardware wallet is an excellent way to understand private keys**, to Specter-DIY, SeedSigner, and Krux independently evolving for years before finally joining forces in São Paulo and sharing responsibility for the same underlying codebase, the same cypherpunk form of self-reliance has run through the entire story.

The premise is that no single supply chain, manufacturer, or semiconductor should be trusted unconditionally.

The alternative is to open the knowledge and tools as widely as possible, allowing anyone willing to make the effort to verify things independently.

It also allows different teams, free from conventional commercial competitive pressures, to borrow from one another and make each other's projects stronger.

The history of DIY hardware signing devices has therefore never simply been a list of device names.

It is the story of how the Bitcoin community repeatedly responded to problems of trust with open-source software, electronic components costing a few dozen dollars, and — ultimately — trust between the people building those tools.

**— End —**

## Reference

[1] Original Chinese article: https://github.com/kdmukai/article-diy-signing-device-summit/blob/main/article.md
